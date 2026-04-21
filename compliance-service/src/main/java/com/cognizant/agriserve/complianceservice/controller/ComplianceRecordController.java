package com.cognizant.agriserve.complianceservice.controller;

import com.cognizant.agriserve.complianceservice.dto.request.ComplianceRecordRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.ComplianceRecordResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord.ComplianceType;
import com.cognizant.agriserve.complianceservice.service.ComplianceRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/compliance-records")
@RequiredArgsConstructor
@Validated
public class ComplianceRecordController {

    private final ComplianceRecordService complianceRecordService;

    @PostMapping
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<ComplianceRecordResponseDTO> createRecord(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @Valid @RequestBody ComplianceRecordRequestDTO requestDTO) {

        log.info("API Request: Officer ID {} is creating a new Compliance Record", officerId);

        ComplianceRecordResponseDTO newRecord = complianceRecordService.createComplianceRecord(officerId, requestDTO);
        return new ResponseEntity<>(newRecord, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<ComplianceRecordResponseDTO>> getAllRecords() {
        log.info("API Request: Fetching all compliance records");
        return ResponseEntity.ok(complianceRecordService.getAllComplianceRecords());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<ComplianceRecordResponseDTO> getRecordById(
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long complianceId) {

        log.info("API Request: Fetching compliance record ID {}", complianceId);
        return ResponseEntity.ok(complianceRecordService.getComplianceRecordById(complianceId));
    }

    @GetMapping("/my-records")
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<List<ComplianceRecordResponseDTO>> getMyRecords(
            @RequestHeader("X-Logged-In-User-Id") Long officerId) {

        log.info("API Request: Fetching compliance records for logged-in Officer ID {}", officerId);
        return ResponseEntity.ok(complianceRecordService.getRecordsByOfficerId(officerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<ComplianceRecordResponseDTO> updateRecord(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long complianceId,
            @Valid @RequestBody ComplianceRecordRequestDTO requestDTO) {

        log.info("API Request: Officer ID {} is updating compliance record ID {}", officerId, complianceId);
        ComplianceRecordResponseDTO updatedRecord = complianceRecordService.updateComplianceRecord(officerId, complianceId, requestDTO);
        return ResponseEntity.ok(updatedRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<Void> deleteRecord(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long complianceId) {

        log.info("API Request: Officer ID {} is deleting compliance record ID {}", officerId, complianceId);
        complianceRecordService.deleteComplianceRecord(complianceId, officerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entity/{entityId}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<ComplianceRecordResponseDTO>> getRecordsByEntity(
            @PathVariable @Positive(message = "ID must be greater than 0") Long entityId) {

        log.info("API Request: Fetching compliance records for Entity ID {}", entityId);
        return ResponseEntity.ok(complianceRecordService.getRecordsByEntity(entityId));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<ComplianceRecordResponseDTO>> getRecordsByType(
            @PathVariable ComplianceType type) {

        log.info("API Request: Fetching compliance records of type {}", type);
        return ResponseEntity.ok(complianceRecordService.getRecordsByType(type));
    }

    @GetMapping("/entity/{entityId}/type/{type}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<ComplianceRecordResponseDTO>> getRecordsByEntityAndType(
            @PathVariable @Positive(message = "ID must be greater than 0") Long entityId,
            @PathVariable ComplianceType type) {

        log.info("API Request: Fetching compliance records for Entity ID {} and type {}", entityId, type);
        return ResponseEntity.ok(complianceRecordService.getRecordsByEntityAndType(entityId, type));
    }
}