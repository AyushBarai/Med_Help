package com.medhelp.common.lab;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medhelp.common.lab.LabUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabUserRepository extends JpaRepository<LabUser, UUID> {
    Optional<LabUser> findByEmail(String email);
    Optional<LabUser> findByIdAndLabId(UUID id, UUID labId);
    List<LabUser> findAllByLabId(UUID labId);
    boolean existsByEmailAndLabId(String email, UUID labId);
}