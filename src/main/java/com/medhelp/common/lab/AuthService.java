package com.medhelp.common.lab;

import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.exception.UnauthorizedException;
import com.medhelp.common.lab.Lab;
import com.medhelp.common.lab.LabRepository;
import com.medhelp.common.lab.LabUser;
import com.medhelp.common.lab.LabUserRepository;
import com.medhelp.common.lab.LabDtos.*;
import com.medhelp.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final LabRepository labRepository;
    private final LabUserRepository labUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ---- LOGIN ----

    public LoginResponse login(LoginRequest request) {
        // 1. Find user by email (across all labs — email is unique globally)
        LabUser user = labUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // 2. Check account is active
        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Contact your lab admin.");
        }

        // 3. Verify password against BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // 4. Verify the lab itself is active
        Lab lab = labRepository.findById(user.getLabId())
                .orElseThrow(() -> new UnauthorizedException("Lab not found"));

        if (!lab.isActive()) {
            throw new UnauthorizedException("Your lab account has been suspended.");
        }

        // 5. Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getLabId(), lab.getId(), user.getRole().name());

        log.info("Login successful for user {} (lab: {})", user.getEmail(), lab.getSlug());

        return new LoginResponse(
                token, "Bearer",
                user.getLabId(), user.getName(), user.getEmail(), user.getRole().name(),
                lab.getId(), lab.getName(), lab.getSlug()
        );
    }

    // ---- LAB SELF-REGISTRATION ----

    @Transactional
    public LabResponse register(LabRegistrationRequest request) {
        // Validate uniqueness
        if (labRepository.existsByPhone(request.phone())) {
            throw new BusinessException("A lab with this phone number already exists");
        }
        if (labRepository.existsByEmail(request.email())) {
            throw new BusinessException("A lab with this email already exists");
        }

        // Generate a URL-safe slug from lab name: "Apollo Diagnostics" → "apollo-diagnostics"
        String slug = generateSlug(request.labName());

        // Create the lab
        Lab lab = Lab.builder()
                .name(request.labName())
                .slug(slug)
                .ownerName(request.ownerName())
                .phone(request.phone())
                .email(request.email())
                .city(request.city())
                .state(request.state())
                .gstin(request.gstin())
                .address(request.address())
                .subscriptionPlan(Lab.SubscriptionPlan.FREE)
                .build();
        lab = labRepository.save(lab);

        // Create the OWNER user
        LabUser owner = LabUser.builder()
                .labId(lab.getId())
                .name(request.ownerName())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(LabUser.Role.OWNER)
                .build();
        labUserRepository.save(owner);

        log.info("New lab registered: {} (slug: {})", lab.getName(), lab.getSlug());

        return LabResponse.from(lab);
    }

    // ---- HELPERS ----

    private String generateSlug(String name) {
        // "Apollo Diagnostics Ltd" → "apollo-diagnostics-ltd"
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")  // remove special chars
                .trim()
                .replaceAll("\\s+", "-");          // spaces to hyphens

        // Ensure uniqueness by appending a number if slug already exists
        String slug = base;
        int counter = 1;
        while (labRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}