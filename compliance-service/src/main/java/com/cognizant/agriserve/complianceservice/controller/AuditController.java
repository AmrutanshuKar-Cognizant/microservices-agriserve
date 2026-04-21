package com.cognizant.agriserve.complianceservice.controller;

import com.cognizant.agriserve.complianceservice.dto.request.AuditRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.AuditResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.Audit.AuditStatus;
import com.cognizant.agriserve.complianceservice.service.AuditService;
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
@RequestMapping("/api/audits")
@RequiredArgsConstructor
@Validated
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<AuditResponseDTO> createAudit(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @Valid @RequestBody AuditRequestDTO requestDTO) {

        log.info("API Request: Officer ID {} is initiating a new Audit", officerId);
        AuditResponseDTO newAudit = auditService.initiateAudit(officerId, requestDTO);
        return new ResponseEntity<>(newAudit, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<AuditResponseDTO>> getAllAudits() {
        log.info("API Request: Fetching all audits");
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<AuditResponseDTO> getAuditById(
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long auditId) {

        log.info("API Request: Fetching audit ID {}", auditId);
        return ResponseEntity.ok(auditService.getAuditById(auditId));
    }

    @GetMapping("/my-audits")
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<List<AuditResponseDTO>> getMyAudits(
            @RequestHeader("X-Logged-In-User-Id") Long officerId) {

        log.info("API Request: Fetching audits for logged-in Officer ID {}", officerId);
        return ResponseEntity.ok(auditService.getAuditsByOfficerId(officerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ComplianceOfficer')")
    public ResponseEntity<AuditResponseDTO> updateAudit(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long auditId,
            @Valid @RequestBody AuditRequestDTO requestDTO) {

        log.info("API Request: Officer ID {} is updating audit ID {}", officerId, auditId);
        AuditResponseDTO updatedAudit = auditService.updateAudit(officerId, auditId, requestDTO);
        return ResponseEntity.ok(updatedAudit);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ComplianceOfficer')") // Or consider hasRole('Admin') if audits shouldn't be deleted easily!
    public ResponseEntity<Void> deleteAudit(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @PathVariable("id") @Positive(message = "ID must be greater than 0") Long auditId) {

        log.info("API Request: Officer ID {} is deleting audit ID {}", officerId, auditId);
        auditService.deleteAudit(auditId, officerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/officer/{officerId}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<AuditResponseDTO>> getAuditsByOfficerId(
            @PathVariable @Positive(message = "ID must be greater than 0") Long officerId) {

        log.info("API Request: Fetching audits for Officer ID {}", officerId);
        return ResponseEntity.ok(auditService.getAuditsByOfficerId(officerId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<AuditResponseDTO>> getAuditsByStatus(@PathVariable AuditStatus status) {
        log.info("API Request: Fetching audits with status {}", status);
        return ResponseEntity.ok(auditService.getAuditsByStatus(status));
    }

    @GetMapping("/officer/{officerId}/status/{status}")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<List<AuditResponseDTO>> getAuditsByOfficerIdAndStatus(
            @PathVariable @Positive(message = "ID must be greater than 0") Long officerId,
            @PathVariable AuditStatus status) {

        log.info("API Request: Fetching audits for Officer ID {} and status {}", officerId, status);
        return ResponseEntity.ok(auditService.getAuditsByOfficerIdAndStatus(officerId, status));
    }
}