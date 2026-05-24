package com.medhelp.common.lab;

import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.lab.Lab;
import com.medhelp.common.lab.LabRepository;
import com.medhelp.common.lab.LabUser;
import com.medhelp.common.lab.LabUserRepository;
import com.medhelp.common.lab.LabDtos.*;
import com.medhelp.common.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabService {

    private final LabRepository labRepository;
    private final LabUserRepository labUserRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- LAB PROFILE ----

    public LabResponse getMyLab() {
        UUID labId = TenantContext.get();
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));
        return LabResponse.from(lab);
    }

    @Transactional
    public LabResponse updateLab(LabRegistrationRequest request) {
        UUID labId = TenantContext.get();
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));

        lab.setName(request.labName());
        lab.setOwnerName(request.ownerName());
        lab.setCity(request.city());
        lab.setState(request.state());
        lab.setGstin(request.gstin());
        lab.setAddress(request.address());

        return LabResponse.from(labRepository.save(lab));
    }

    // ---- STAFF MANAGEMENT ----

    public List<StaffResponse> getStaff() {
        UUID labId = TenantContext.get();
        return labUserRepository.findAllByLabId(labId)
                .stream()
                .map(StaffResponse::from)
                .toList();
    }

    @Transactional
    public StaffResponse addStaff(AddStaffRequest request) {
        UUID labId = TenantContext.get();

        if (labUserRepository.existsByEmailAndLabId(request.email(), labId)) {
            throw new BusinessException("A staff member with this email already exists in your lab");
        }

        LabUser user = LabUser.builder()
                .labId(labId)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return StaffResponse.from(labUserRepository.save(user));
    }

    @Transactional
    public StaffResponse toggleStaffStatus(UUID userId) {
        UUID labId = TenantContext.get();
        LabUser user = labUserRepository.findByIdAndLabId(userId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member", userId));

        // Prevent owner from deactivating themselves
        if (user.getRole() == LabUser.Role.OWNER) {
            throw new BusinessException("Cannot deactivate the lab owner account");
        }

        user.setActive(!user.isActive());
        return StaffResponse.from(labUserRepository.save(user));
    }
}