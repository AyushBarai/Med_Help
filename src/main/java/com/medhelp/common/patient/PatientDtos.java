package com.medhelp.common.patient;

import com.medhelp.common.patient.Patient;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

public class PatientDtos {

    // ---- CREATE / UPDATE ----

    public record CreatePatientRequest(
            @NotBlank(message = "Name is required")
            String name,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
            String phone,

            @Email(message = "Invalid email")
            String email,

            LocalDate dob,

            Patient.Gender gender,

            String address
    ) {}

    public record UpdatePatientRequest(
            String name,
            @Email String email,
            LocalDate dob,
            Patient.Gender gender,
            String address
    ) {}

    // ---- RESPONSE ----

    public record PatientResponse(
            UUID id,
            String name,
            String phone,
            String email,
            LocalDate dob,
            Integer age,           // calculated from dob
            String gender,
            String address,
            String createdAt
    ) {
        public static PatientResponse from(Patient p) {
            Integer age = null;
            if (p.getDob() != null) {
                age = Period.between(p.getDob(), LocalDate.now()).getYears();
            }
            return new PatientResponse(
                    p.getLabId(), p.getName(), p.getPhone(), p.getEmail(),
                    p.getDob(), age,
                    p.getGender() != null ? p.getGender().name() : null,
                    p.getAddress(),
                    p.getCreatedDate() != null ? p.getCreatedDate().toString() : null
            );
        }
    }
}