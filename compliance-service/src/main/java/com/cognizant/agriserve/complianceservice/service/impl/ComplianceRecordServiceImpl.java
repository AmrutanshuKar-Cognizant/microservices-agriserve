package com.cognizant.agriserve.complianceservice.service.impl;

import com.cognizant.agriserve.complianceservice.client.AdvisoryClient;
import com.cognizant.agriserve.complianceservice.client.TrainingClient;
import com.cognizant.agriserve.complianceservice.client.UserClient;
import com.cognizant.agriserve.complianceservice.dto.request.ComplianceRecordRequestDTO;
import com.cognizant.agriserve.complianceservice.dto.response.ComplianceRecordResponseDTO;
import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord;
import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord.ComplianceType;
import com.cognizant.agriserve.complianceservice.dao.ComplianceRecordRepository;
import com.cognizant.agriserve.complianceservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.complianceservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.complianceservice.service.ComplianceRecordService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceRecordServiceImpl implements ComplianceRecordService {

    private final ComplianceRecordRepository repository;
    private final UserClient userClient;
    private final TrainingClient trainingClient;
    private final AdvisoryClient advisoryClient;
    private final ModelMapper modelMapper;

    @Override
    public ComplianceRecordResponseDTO createComplianceRecord(Long officerId, ComplianceRecordRequestDTO requestDTO) {
        log.info("Officer ID {} is attempting to create a new {} record for Entity ID {}",
                officerId, requestDTO.getType(), requestDTO.getEntityId());

        // Any UnauthorizedActionException or ResourceNotFoundException will automatically
        // bubble up to the Global Exception Handler. No try-catch needed here!
        validateEntityExists(requestDTO.getEntityId(), requestDTO.getType(), officerId);

        ComplianceRecord record = mapToEntity(requestDTO);
        record.setComplianceId(null);
        record.setOfficerId(officerId);

        ComplianceRecord savedRecord = repository.save(record);
        log.info("Successfully created Compliance Record ID {} for Officer ID {}", savedRecord.getComplianceId(), officerId);

        return mapToResponseDTO(savedRecord);
    }

    @Override
    public ComplianceRecordResponseDTO getComplianceRecordById(Long complianceId) {
        log.debug("Fetching Compliance Record with ID: {}", complianceId);

        ComplianceRecord record = repository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record not found with ID: " + complianceId));
        return mapToResponseDTO(record);
    }

    @Override
    public List<ComplianceRecordResponseDTO> getAllComplianceRecords() {
        log.debug("Fetching all Compliance Records");

        return repository.findAllByOrderByDateDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplianceRecordResponseDTO> getRecordsByEntity(Long entityId) {
        log.debug("Fetching Compliance Records for Entity ID: {}", entityId);

        return repository.findByEntityId(entityId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplianceRecordResponseDTO> getRecordsByType(ComplianceType type) {
        log.debug("Fetching Compliance Records of Type: {}", type);

        return repository.findByType(type)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplianceRecordResponseDTO> getRecordsByEntityAndType(Long entityId, ComplianceType type) {
        log.debug("Fetching Compliance Records for Entity ID: {} and Type: {}", entityId, type);

        return repository.findByEntityIdAndType(entityId, type)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplianceRecordResponseDTO> getRecordsByOfficerId(Long officerId) {
        log.debug("Fetching Compliance Records for Officer ID: {}", officerId);

        return repository.findByOfficerId(officerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComplianceRecordResponseDTO updateComplianceRecord(Long officerId, Long complianceId, ComplianceRecordRequestDTO requestDTO) {
        log.info("Officer ID {} is attempting to update Compliance Record ID {}", officerId, complianceId);

        ComplianceRecord existingRecord = repository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record not found with ID: " + complianceId));

        if (!existingRecord.getOfficerId().equals(officerId)) {
            // This exception will also be caught perfectly by our Global Exception Handler!
            throw new UnauthorizedActionException("Access Denied: You can only edit your own compliance records.");
        }

        validateEntityExists(requestDTO.getEntityId(), requestDTO.getType(), officerId);

        existingRecord.setEntityId(requestDTO.getEntityId());
        existingRecord.setType(requestDTO.getType());
        existingRecord.setResult(requestDTO.getResult());
        existingRecord.setNotes(requestDTO.getNotes());

        ComplianceRecord updatedRecord = repository.save(existingRecord);
        log.info("Successfully updated Compliance Record ID {}", updatedRecord.getComplianceId());

        return mapToResponseDTO(updatedRecord);
    }

    @Override
    public void deleteComplianceRecord(Long complianceId, Long officerId) {
        log.info("Officer ID {} is attempting to DELETE Compliance Record ID {}", officerId, complianceId);

        ComplianceRecord existingRecord = repository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record not found with ID: " + complianceId));

        if (!existingRecord.getOfficerId().equals(officerId)) {
            throw new UnauthorizedActionException("Access Denied: You can only delete your own compliance records.");
        }

        repository.deleteById(complianceId);
        log.info("Successfully deleted Compliance Record ID {}", complianceId);
    }

    private void validateEntityExists(Long targetId, ComplianceType type, Long officerId) {
        if (type == ComplianceType.TRAINING) {
            try {
                trainingClient.checkProgramExists(targetId);
            } catch (FeignException.NotFound e) {
                log.warn("Validation failed: Training Program ID {} does not exist.", targetId);
                throw new ResourceNotFoundException("No Training Program found with ID: " + targetId);
            } catch (FeignException e) {
                log.error("CRITICAL FEIGN ERROR: Status = {}, Body = {}", e.status(), e.contentUTF8());
                throw new RuntimeException("Failed to communicate with Training Service. Status: " + e.status());
            } catch (Exception e) {
                log.error("Could not reach Training Service", e);
                throw new RuntimeException("Training Service is currently unreachable.");
            }
        } else if (type == ComplianceType.ADVISORY) {
            try {
                advisoryClient.verifyAdvisorySessionExists(targetId);
            } catch (FeignException.NotFound e) {
                log.warn("Validation failed: Advisory Session ID {} does not exist.", targetId);
                throw new ResourceNotFoundException("No Advisory Session found with ID: " + targetId);
            } catch (FeignException e) {
                log.error("CRITICAL FEIGN ERROR: Status = {}, Body = {}", e.status(), e.contentUTF8());
                throw new RuntimeException("Failed to communicate with Advisory Service. Status: " + e.status());
            } catch (Exception e) {
                log.error("Could not reach Advisory Service", e);
                throw new RuntimeException("Advisory Service is currently unreachable.");
            }
        }
    }

    private ComplianceRecord mapToEntity(ComplianceRecordRequestDTO dto) {
        return modelMapper.map(dto, ComplianceRecord.class);
    }

    private ComplianceRecordResponseDTO mapToResponseDTO(ComplianceRecord record) {
        ComplianceRecordResponseDTO dto = modelMapper.map(record, ComplianceRecordResponseDTO.class);
        enrichWithOfficerName(dto);
        return dto;
    }

    private void enrichWithOfficerName(ComplianceRecordResponseDTO dto) {
        try {
            String name = userClient.getOfficerName(dto.getOfficerId());
            dto.setOfficerName(name);
        } catch (Exception e) {
            dto.setOfficerName("Unknown Officer (Service Unavailable)");
        }
    }
}