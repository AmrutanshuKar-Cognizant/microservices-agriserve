package com.cognizant.agriserve.complianceservice.service;

import com.cognizant.agriserve.complianceservice.dto.request.AuditRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.AuditResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.Audit.AuditStatus;

import java.util.List;

public interface AuditService {


    AuditResponseDTO initiateAudit(Long officerId, AuditRequestDTO requestDTO);

    AuditResponseDTO getAuditById(Long auditId);

    List<AuditResponseDTO> getAllAudits();

    List<AuditResponseDTO> getAuditsByOfficerId(Long officerId);

    List<AuditResponseDTO> getAuditsByStatus(AuditStatus status);

    List<AuditResponseDTO> getAuditsByOfficerIdAndStatus(Long officerId, AuditStatus status);

    AuditResponseDTO updateAudit(Long officerId, Long auditId, AuditRequestDTO requestDTO);

    void deleteAudit(Long officerId, Long auditId);
}