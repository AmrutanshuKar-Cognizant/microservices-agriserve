package com.cognizant.agriserve.farmerservice.service;

import com.cognizant.agriserve.farmerservice.dto.request.FarmerDocumentUploadRequestDTO;
import com.cognizant.agriserve.farmerservice.dto.response.FarmerDocumentResponseDTO;

import java.util.List;

public interface FarmerDocumentService {

    FarmerDocumentResponseDTO uploadDocument(Long userId, FarmerDocumentUploadRequestDTO requestDto);

    List<FarmerDocumentResponseDTO> getDocumentsByUserId(Long userId);

    List<FarmerDocumentResponseDTO> getAllPendingDocuments();

    FarmerDocumentResponseDTO updateDocumentStatus(Long documentId, String newStatus);
}