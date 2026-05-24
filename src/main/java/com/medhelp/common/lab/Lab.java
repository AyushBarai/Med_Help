package com.medhelp.common.lab;

import com.medhelp.common.medhelp.Baseentity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "labs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class Lab extends Baseentity {
 
    @Column(nullable = false)
    private String name;
 
    // Used in patient portal URL: reports.pathlab.com/apollo-demo
    @Column(unique = true, nullable = false)
    private String slug;
 
    @Column(nullable = false)
    private String ownerName;
 
    @Column(nullable = false, unique = true)
    private String phone;
 
    @Column(unique = true)
    private String email;
 
    @Column(columnDefinition = "TEXT")
    private String address;
 
    private String city;
    private String state;
    private String gstin;
    private String logoUrl;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;
 
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
 
    public enum SubscriptionPlan { FREE, BASIC, PRO }
}
