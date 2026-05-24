package com.medhelp.common.patient;

import com.medhelp.common.patient.PatientDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient registration and lookup")
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "Register a new patient")
    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.register(request));
    }

    @Operation(summary = "Search patients by name or phone — used at reception")
    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(patientService.search(q));
    }

    @Operation(summary = "Get a single patient by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @Operation(summary = "Update patient details")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }
}