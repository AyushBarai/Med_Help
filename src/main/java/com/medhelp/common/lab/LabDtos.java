package com.medhelp.common.lab;

import jakarta.validation.constraints.*;
import java.util.UUID;

import com.medhelp.common.lab.Lab;
import com.medhelp.common.lab.LabUser;

// =====================================================================
//  All DTOs (Data Transfer Objects) for the Lab module in one file.
//  DTOs are what the API receives (Request) and sends back (Response).
//  We use Java Records — immutable, no boilerplate, perfect for DTOs.
// =====================================================================

// ---- LOGIN ----

public class LabDtos {

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record LoginResponse(
            String accessToken,
            String tokenType,          // always "Bearer"
            UUID userId,
            String userName,
            String email,
            String role,
            UUID labId,
            String labName,
            String labSlug
    ) {}

    // ---- LAB REGISTRATION ----

    public record LabRegistrationRequest(
            @NotBlank(message = "Lab name is required")
            String labName,

            @NotBlank(message = "Owner name is required")
            String ownerName,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
            String phone,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            String city,
            String state,
            String gstin,
            String address
    ) {}

    // ---- STAFF MANAGEMENT ----

    public record AddStaffRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @Pattern(regexp = "^[0-9]{10}$") String phone,
            @NotBlank @Size(min = 8) String password,
            @NotNull LabUser.Role role
    ) {}

    // ---- RESPONSES ----

    public record LabResponse(
            UUID id,
            String name,
            String slug,
            String ownerName,
            String phone,
            String email,
            String city,
            String state,
            String logoUrl,
            String subscriptionPlan
    ) {
        // Static factory — converts entity to response DTO
        public static LabResponse from(Lab lab) {
            return new LabResponse(
                    lab.getId(), lab.getName(), lab.getSlug(),
                    lab.getOwnerName(), lab.getPhone(), lab.getEmail(),
                    lab.getCity(), lab.getState(), lab.getLogoUrl(),
                    lab.getSubscriptionPlan().name()
            );
        }
    }

    public record StaffResponse(
            UUID id,
            String name,
            String email,
            String phone,
            String role,
            boolean isActive
    ) {
        public static StaffResponse from(LabUser user) {
            return new StaffResponse(
                    user.getLabId(), user.getName(), user.getEmail(),
                    user.getPhone(), user.getRole().name(), user.isActive()
            );
        }
    }
}