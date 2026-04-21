package com.cognizant.agriserve.complianceservice.service.impl;

import com.cognizant.agriserve.complianceservice.client.UserClient;
import com.cognizant.agriserve.complianceservice.dao.AuditRepository;
import com.cognizant.agriserve.complianceservice.dto.request.AuditRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.AuditResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.Audit;
import com.cognizant.agriserve.complianceservice.entity.Audit.AuditStatus;
import com.cognizant.agriserve.complianceservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.complianceservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.complianceservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final UserClient userClient;
    private final ModelMapper modelMapper;

    @Override
    public AuditResponseDTO initiateAudit(Long officerId, AuditRequestDTO requestDTO) {
        log.info("Officer ID {} is initiating a new Audit", officerId);

        Audit audit = modelMapper.map(requestDTO, Audit.class);
        audit.setAuditId(null);
        audit.setOfficerId(officerId); // Secured via API Gateway header

        // Note: The date is automatically handled by your @PrePersist onCreate() method!

        Audit savedAudit = auditRepository.save(audit);
        log.info("Successfully initiated Audit ID {}", savedAudit.getAuditId());

        return mapToResponseDTO(savedAudit);
    }

    @Override
    public AuditResponseDTO getAuditById(Long auditId) {
        log.debug("Fetching Audit ID: {}", auditId);
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found with ID: " + auditId));
        return mapToResponseDTO(audit);
    }

    @Override
    public List<AuditResponseDTO> getAllAudits() {
        log.debug("Fetching all Audits");
        return auditRepository.findAllByOrderByDateDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditResponseDTO> getAuditsByOfficerId(Long officerId) {
        log.debug("Fetching Audits for Officer ID: {}", officerId);
        return auditRepository.findByOfficerId(officerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditResponseDTO> getAuditsByStatus(AuditStatus status) {
        log.debug("Fetching Audits with status: {}", status);
        return auditRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditResponseDTO> getAuditsByOfficerIdAndStatus(Long officerId, AuditStatus status) {
        log.debug("Fetching Audits for Officer ID: {} with status: {}", officerId, status);
        return auditRepository.findByOfficerIdAndStatus(officerId, status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AuditResponseDTO updateAudit(Long officerId, Long auditId, AuditRequestDTO requestDTO) {
        log.info("Officer ID {} attempting to update Audit ID {}", officerId, auditId);

        Audit existingAudit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found with ID: " + auditId));

        // 1. Security Check: Ownership
        if (!existingAudit.getOfficerId().equals(officerId)) {
            log.warn("SECURITY BLOCKED: Officer ID {} attempted to update Audit ID {} owned by Officer {}",
                    officerId, auditId, existingAudit.getOfficerId());
            throw new UnauthorizedActionException("Access Denied: You can only edit your own audits.");
        }

        // 2. Business Rule: The Immutable Vault
        if (existingAudit.getStatus() == AuditStatus.COMPLETED) {
            log.warn("BUSINESS RULE BLOCKED: Officer {} attempted to modify completed Audit {}", officerId, auditId);
            throw new IllegalStateException("This audit has been marked as COMPLETED and cannot be modified.");
        }

        // Apply updates
        existingAudit.setScope(requestDTO.getScope());
        existingAudit.setFindings(requestDTO.getFindings());
        existingAudit.setStatus(requestDTO.getStatus());

        Audit updatedAudit = auditRepository.save(existingAudit);
        log.info("Successfully updated Audit ID {}", updatedAudit.getAuditId());

        return mapToResponseDTO(updatedAudit);
    }

    @Override
    public void deleteAudit(Long auditId, Long officerId) { // Notice the updated signature!
        log.info("Officer ID {} attempting to delete Audit ID {}", officerId, auditId);

        Audit existingAudit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found with ID: " + auditId));

        // 1. Security Check: Ownership
        if (!existingAudit.getOfficerId().equals(officerId)) {
            log.warn("SECURITY BLOCKED: Officer ID {} attempted to delete Audit ID {} owned by Officer {}",
                    officerId, auditId, existingAudit.getOfficerId());
            throw new UnauthorizedActionException("Access Denied: You can only delete your own audits.");
        }

        // 2. Business Rule: The Immutable Vault
        if (existingAudit.getStatus() == AuditStatus.COMPLETED) {
            log.warn("BUSINESS RULE BLOCKED: Officer {} attempted to delete completed Audit {}", officerId, auditId);
            throw new IllegalStateException("This audit is part of the historical record (COMPLETED) and cannot be deleted.");
        }

        auditRepository.deleteById(auditId);
        log.info("Successfully deleted Audit ID {}", auditId);
    }


    private AuditResponseDTO mapToResponseDTO(Audit audit) {
        AuditResponseDTO dto = modelMapper.map(audit, AuditResponseDTO.class);

        enrichWithOfficerName(dto);

        return dto;
    }

    private void enrichWithOfficerName(AuditResponseDTO dto) {
        try {
            String name = userClient.getOfficerName(dto.getOfficerId());
            dto.setOfficerName(name);
        } catch (Exception e) {
            log.error("Failed to fetch officer name for ID {}: {}", dto.getOfficerId(), e.getMessage());
            dto.setOfficerName("Unknown Officer");
        }
    }
}