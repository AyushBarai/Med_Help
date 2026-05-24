package com.medhelp.common.lab;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import com.medhelp.common.medhelp.Baseentity;

@Entity
@Table(name = "lab_users",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "email"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabUser extends Baseentity {

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    public enum Role { OWNER, DOCTOR, TECHNICIAN, RECEPTIONIST }
}