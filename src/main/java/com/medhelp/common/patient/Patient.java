package com.medhelp.common.patient;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "patients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "phone"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient extends Baseentity {

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Generated when patient accesses portal — one-time token for OTP-less deep link
    private String portalAccessToken;

    public enum Gender { MALE, FEMALE, OTHER }
}