package com.cognizant.agriserve.farmerservice.controller;

import com.cognizant.agriserve.farmerservice.dto.response.FarmerDocumentResponseDTO;
import com.cognizant.agriserve.farmerservice.dto.request.FarmerDocumentUploadRequestDTO;
import com.cognizant.agriserve.farmerservice.service.FarmerDocumentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/farmers/documents")
@RequiredArgsConstructor
public class FarmerDocumentController {

    private final FarmerDocumentService farmerDocumentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<FarmerDocumentResponseDTO> uploadDocument(
            @RequestHeader("X-Logged-In-User-Id") Long userId,
            @Valid @RequestBody FarmerDocumentUploadRequestDTO requestDto) {

        log.info("API Request: Uploading new document for Farmer ID: {}", requestDto.getFarmerId());
        FarmerDocumentResponseDTO uploadedDocument = farmerDocumentService.uploadDocument(userId, requestDto);

        return new ResponseEntity<>(uploadedDocument, HttpStatus.CREATED);
    }

    @GetMapping("/my-documents")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<List<FarmerDocumentResponseDTO>> getMyDocuments(
            @RequestHeader("X-Logged-In-User-Id") Long userId) {

        log.info("API Request: Fetching documents for User ID: {}", userId);
        List<FarmerDocumentResponseDTO> documents = farmerDocumentService.getDocumentsByUserId(userId);

        return ResponseEntity.ok(documents);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<FarmerDocumentResponseDTO>> getAllPendingDocuments() {
        log.info("API Request: Fetching all pending documents for review");
        List<FarmerDocumentResponseDTO> documents = farmerDocumentService.getAllPendingDocuments();
        return ResponseEntity.ok(documents);
    }

    @PatchMapping("/{documentId}/status")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<FarmerDocumentResponseDTO> updateStatus(
            @PathVariable Long documentId,
            @RequestParam String status) {

        log.info("API Request: Updating status for Document ID: {} to {}", documentId, status);
        FarmerDocumentResponseDTO updated = farmerDocumentService.updateDocumentStatus(documentId, status);
        return ResponseEntity.ok(updated);
    }
}