package com.cognizant.agriserve.feedbackandsatisfactionservice.service.impl;

import com.cognizant.agriserve.feedbackandsatisfactionservice.client.TrainingProgramClient;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricResponseDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.entity.Feedback;
import com.cognizant.agriserve.feedbackandsatisfactionservice.entity.SatisfactionMetric;
import com.cognizant.agriserve.feedbackandsatisfactionservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.TrainingProgramDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.repo.FeedbackRepository;
import com.cognizant.agriserve.feedbackandsatisfactionservice.repo.SatisfactionMetricRepository;
import com.cognizant.agriserve.feedbackandsatisfactionservice.service.SatisfactionMetricService;
import feign.FeignException; // <-- Added FeignException import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SatisfactionMetricImpl implements SatisfactionMetricService {

    @Autowired
    private SatisfactionMetricRepository metricRepo;
    @Autowired
    private FeedbackRepository feedbackRepo;
    @Autowired
    private TrainingProgramClient programClient;

    @Override
    public SatisfactionMetricResponseDTO evaluate(SatisfactionMetricRequestDTO dto) {
        log.info("Evaluating Metrics for Program ID: {}", dto.getProgramId());

        // 1. Extract role in a single, final assignment
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String currentRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SERVICE";

        // 2. Validate and Fetch External Entity using Strict Exception Handling
        TrainingProgramDTO program = fetchTrainingProgramSafely(dto.getProgramId(), currentRole);

        // 3. FETCH: Local feedback data for this program ID
        List<Feedback> feedbackList = feedbackRepo.findByTrainingProgramId(dto.getProgramId());

        if (feedbackList.isEmpty()) {
            log.warn("No feedback entries found for Program: {}", dto.getProgramId());
            // Upgraded to custom exception so GlobalExceptionHandler catches it cleanly
            throw new ResourceNotFoundException("Cannot evaluate: No feedback exists for this program yet.");
        }

        // 4. CALCULATE
        double averageScore = feedbackList.stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);

        // 5. SAVE
        SatisfactionMetric metric = new SatisfactionMetric();
        metric.setTrainingProgramId(dto.getProgramId());
        metric.setScore(averageScore);
        metric.setStatus(dto.getStatus() != null ? dto.getStatus() : "EVALUATED");

        SatisfactionMetric savedMetric = metricRepo.save(metric);

        // 6. RETURN
        return SatisfactionMetricResponseDTO.builder()
                .programId(savedMetric.getTrainingProgramId())
                // .programName(program.getTitle()) // <-- Uncomment if your DTO has this field!
                .status(savedMetric.getStatus())
                .score(savedMetric.getScore())
                .build();
    }

    @Override
    public List<SatisfactionMetricResponseDTO> getSatisfactionmetric() {
        // 1. Extract role in a single, final assignment
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String currentRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SERVICE";

        List<SatisfactionMetric> entities = metricRepo.findAll();

        // 2. Cache Map to prevent duplicate Feign calls for the same program ID
        Map<Long, String> programNames = new HashMap<>();

        return entities.stream().map(entity -> {

            // 3. Graceful Degradation: computeIfAbsent + try/catch
            String pName = programNames.computeIfAbsent(entity.getTrainingProgramId(), id -> {
                try {
                    return programClient.getProgramById(id).getTitle();
                } catch (Exception e) {
                    log.warn("Could not fetch Program ID: {}. Service unavailable or missing.", id);
                    return "Unknown Program (Service Unavailable)";
                }
            });

            return SatisfactionMetricResponseDTO.builder()
                    .programId(entity.getTrainingProgramId())
                    // .programName(pName) // <-- Uncomment if your DTO has this field!
                    .status(entity.getStatus())
                    .score(entity.getScore())
                    .build();
        }).toList();
    }

    // =========================================================================
    // PRIVATE FEIGN VALIDATION HELPER METHOD
    // =========================================================================

    private TrainingProgramDTO fetchTrainingProgramSafely(Long programId, String currentRole) {
        try {
            return programClient.getProgramById(programId);
        } catch (FeignException.NotFound e) {
            log.warn("Validation failed: Training Program ID {} does not exist.", programId);
            throw new ResourceNotFoundException("No Training Program found with ID: " + programId);
        } catch (FeignException e) {
            log.error("CRITICAL FEIGN ERROR: Status = {}, Body = {}", e.status(), e.contentUTF8());
            throw new RuntimeException("Failed to communicate with Training Service. Status: " + e.status());
        } catch (Exception e) {
            log.error("Could not reach Training Service", e);
            throw new RuntimeException("Training Service is currently unreachable.");
        }
    }
}