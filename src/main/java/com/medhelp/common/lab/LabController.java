package com.medhelp.common.lab;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.medhelp.common.lab.AuthService;
import com.medhelp.common.lab.LabService;
import com.medhelp.common.lab.LabDtos.*;

import java.util.List;
import java.util.UUID;

// =====================================================================
//  AuthController  →  /api/v1/auth/**  (public — no JWT needed)
// =====================================================================

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and lab registration")
class AuthController {

    private final AuthService authService;

    @Operation(summary = "Staff login — returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Register a new lab (self-onboarding)")
    @PostMapping("/register")
    public ResponseEntity<LabResponse> register(@Valid @RequestBody LabRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}

// =====================================================================
//  LabController  →  /api/v1/lab/**  (requires JWT)
// =====================================================================

@RestController
@RequestMapping("/api/v1/lab")
@RequiredArgsConstructor
@Tag(name = "Lab Management", description = "Lab profile and staff management")
class LabController {

    private final LabService labService;

    @Operation(summary = "Get current lab profile")
    @GetMapping
    public ResponseEntity<LabResponse> getMyLab() {
        return ResponseEntity.ok(labService.getMyLab());
    }

    @Operation(summary = "Update lab profile")
    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<LabResponse> updateLab(@Valid @RequestBody LabRegistrationRequest request) {
        return ResponseEntity.ok(labService.updateLab(request));
    }

    @Operation(summary = "List all staff members")
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('OWNER', 'DOCTOR')")
    public ResponseEntity<List<StaffResponse>> getStaff() {
        return ResponseEntity.ok(labService.getStaff());
    }

    @Operation(summary = "Add a new staff member")
    @PostMapping("/staff")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<StaffResponse> addStaff(@Valid @RequestBody AddStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labService.addStaff(request));
    }

    @Operation(summary = "Activate or deactivate a staff member")
    @PatchMapping("/staff/{userId}/toggle")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<StaffResponse> toggleStaff(@PathVariable UUID userId) {
        return ResponseEntity.ok(labService.toggleStaffStatus(userId));
    }
}