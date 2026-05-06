package com.cognizant.agriserve.farmerservice.service.impl;

import com.cognizant.agriserve.farmerservice.dao.FarmerDocumentRepository;
import com.cognizant.agriserve.farmerservice.dao.FarmerRepository;
import com.cognizant.agriserve.farmerservice.dto.request.FarmerDocumentUploadRequestDTO;
import com.cognizant.agriserve.farmerservice.dto.response.FarmerDocumentResponseDTO;
import com.cognizant.agriserve.farmerservice.entity.Farmer;
import com.cognizant.agriserve.farmerservice.entity.FarmerDocument;
import com.cognizant.agriserve.farmerservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.farmerservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.farmerservice.service.FarmerDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerDocumentServiceImpl implements FarmerDocumentService {

    private final FarmerDocumentRepository farmerDocumentRepository;
    private final FarmerRepository farmerRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FarmerDocumentResponseDTO uploadDocument(Long userId, FarmerDocumentUploadRequestDTO dto) {
        // Log using the numeric ID provided in the request
        log.info("Starting document upload process for Farmer ID: {}", dto.getFarmerId());

        // 1. Verify Farmer existence within the local database
        Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + dto.getFarmerId()));

        if (!farmer.getUserId().equals(userId)) {
            log.warn("SECURITY ALERT: User ID {} attempted to upload a document for Farmer ID {}",
                    userId, farmer.getFarmerId());

            // Throwing this specific exception tells Spring to return a 403 Forbidden status
            throw new UnauthorizedActionException("You are not authorized to upload documents for this farmer profile.");
        }

        // 2. Map DTO to Entity using Builder for clarity
        FarmerDocument document = FarmerDocument.builder()
                .docType(dto.getDocType())
                .fileURI(dto.getFileURI())
                .uploadedDate(LocalDate.now())
                .verificationStatus(FarmerDocument.VerificationStatus.PENDING)
                .farmer(farmer)
                .build();

        // 3. Persist and return
        FarmerDocument savedDocument = farmerDocumentRepository.save(document);
        log.info("Document successfully persisted. DocumentID: {} | FarmerID: {}",
                savedDocument.getDocumentId(), farmer.getFarmerId());

        return mapToResponse(savedDocument);
    }

    @Override
    public List<FarmerDocumentResponseDTO> getDocumentsByUserId(Long userId) {
        log.debug("Retrieving documents for User ID: {}", userId);

        // 1. Find the internal Farmer record linked to this external Auth User ID
        Farmer farmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No farmer profile linked to User ID: " + userId));

        // 2. Fetch documents using the local relationship
        List<FarmerDocument> documents = farmerDocumentRepository.findByFarmer_FarmerId(farmer.getFarmerId());

        log.debug("Found {} documents for Farmer ID: {}", documents.size(), farmer.getFarmerId());

        return documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FarmerDocumentResponseDTO> getAllPendingDocuments() {
        log.info("Administrative fetch: Retrieving all PENDING documents across the service");

        return farmerDocumentRepository.findByVerificationStatus(FarmerDocument.VerificationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FarmerDocumentResponseDTO updateDocumentStatus(Long documentId, String newStatus) {
        log.info("Status Update Request - DocID: {} | Target Status: {}", documentId, newStatus);

        FarmerDocument document = farmerDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document ID " + documentId + " not found"));

        try {
            FarmerDocument.VerificationStatus statusEnum =
                    FarmerDocument.VerificationStatus.valueOf(newStatus.toUpperCase());
            document.setVerificationStatus(statusEnum);

            log.info("Document {} status updated to {}", documentId, statusEnum);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status transition attempt: {}", newStatus);
            throw new RuntimeException("Invalid status. Must be PENDING, VERIFIED, or REJECTED.");
        }

        return mapToResponse(farmerDocumentRepository.save(document));
    }

    private FarmerDocumentResponseDTO mapToResponse(FarmerDocument document) {
        FarmerDocumentResponseDTO response = modelMapper.map(document, FarmerDocumentResponseDTO.class);
        // Ensure the ID link is preserved in the response for frontend consumption
        response.setFarmerId(document.getFarmer().getFarmerId());
        return response;
    }
}