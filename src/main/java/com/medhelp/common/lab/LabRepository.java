package com.medhelp.common.lab;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medhelp.common.lab.Lab;

import java.util.Optional;
import java.util.UUID;

public interface LabRepository extends JpaRepository<Lab, UUID> {
    Optional<Lab> findBySlug(String slug);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
}