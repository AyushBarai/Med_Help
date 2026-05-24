package com.medhelp.common.patient;

import com.medhelp.common.patient.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndLabId(UUID id, UUID labId);

    Optional<Patient> findByPhoneAndLabId(String phone, UUID labId);

    boolean existsByPhoneAndLabId(String phone, UUID labId);

    // Search by name OR phone — used at the reception desk lookup
    @Query("""
        SELECT p FROM Patient p
        WHERE p.labId = :labId
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR p.phone LIKE CONCAT('%', :query, '%'))
        ORDER BY p.createdDate DESC
    """)
    List<Patient> searchByNameOrPhone(String query, UUID labId);

    // For patient portal — find by phone to send OTP
    Optional<Patient> findByPhone(String phone);
}