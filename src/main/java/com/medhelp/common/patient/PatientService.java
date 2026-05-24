package com.medhelp.common.patient;

import com.medhelp.common.exception.BusinessException;
import com.medhelp.common.exception.ResourceNotFoundException;
import com.medhelp.common.tenant.TenantContext;
import com.medhelp.common.patient.PatientDtos.*;
import com.medhelp.common.patient.Patient;
import com.medhelp.common.patient.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    // ---- REGISTER ----

    @Transactional
    public PatientResponse register(CreatePatientRequest request) {
        UUID labId = TenantContext.get();

        // Phone must be unique per lab
        if (patientRepository.existsByPhoneAndLabId(request.phone(), labId)) {
            throw new BusinessException("Patient with phone " + request.phone() + " already exists in this lab");
        }

        Patient patient = Patient.builder()
                .labId(labId)
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .dob(request.dob())
                .gender(request.gender())
                .address(request.address())
                .build();

        return PatientResponse.from(patientRepository.save(patient));
    }

    // ---- SEARCH (reception desk lookup) ----

    public List<PatientResponse> search(String query) {
        UUID labId = TenantContext.get();
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return patientRepository.searchByNameOrPhone(query.trim(), labId)
                .stream()
                .map(PatientResponse::from)
                .toList();
    }

    // ---- GET ONE ----

    public PatientResponse getById(UUID patientId) {
        UUID labId = TenantContext.get();
        Patient patient = patientRepository.findByIdAndLabId(patientId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        return PatientResponse.from(patient);
    }

    // ---- UPDATE ----

    @Transactional
    public PatientResponse update(UUID patientId, UpdatePatientRequest request) {
        UUID labId = TenantContext.get();
        Patient patient = patientRepository.findByIdAndLabId(patientId, labId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        // Only update non-null fields
        if (request.name()    != null) patient.setName(request.name());
        if (request.email()   != null) patient.setEmail(request.email());
        if (request.dob()     != null) patient.setDob(request.dob());
        if (request.gender()  != null) patient.setGender(request.gender());
        if (request.address() != null) patient.setAddress(request.address());

        return PatientResponse.from(patientRepository.save(patient));
    }
}