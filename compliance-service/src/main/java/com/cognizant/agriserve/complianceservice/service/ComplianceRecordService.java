package com.cognizant.agriserve.complianceservice.service;

import com.cognizant.agriserve.complianceservice.dto.request.ComplianceRecordRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.ComplianceRecordResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord.ComplianceType;

import java.util.List;

public interface ComplianceRecordService {

    ComplianceRecordResponseDTO createComplianceRecord(Long officerId, ComplianceRecordRequestDTO requestDTO);

    ComplianceRecordResponseDTO getComplianceRecordById(Long complianceId);

    List<ComplianceRecordResponseDTO> getAllComplianceRecords();

    List<ComplianceRecordResponseDTO> getRecordsByEntity(Long entityId);

    List<ComplianceRecordResponseDTO> getRecordsByType(ComplianceType type);

    List<ComplianceRecordResponseDTO> getRecordsByEntityAndType(Long entityId, ComplianceType type);

    List<ComplianceRecordResponseDTO> getRecordsByOfficerId(Long officerId);

    ComplianceRecordResponseDTO updateComplianceRecord(Long officerId, Long complianceId, ComplianceRecordRequestDTO requestDTO);

    void deleteComplianceRecord(Long complianceId, Long officerId);
}