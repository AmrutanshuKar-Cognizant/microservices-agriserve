package com.cognizant.agriserve.feedbackandsatisfactionservice.service.impl;

import com.cognizant.agriserve.feedbackandsatisfactionservice.client.AdvisorySessionClient;
import com.cognizant.agriserve.feedbackandsatisfactionservice.client.FarmerClient;
import com.cognizant.agriserve.feedbackandsatisfactionservice.client.TrainingProgramClient;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.FeedbackResponseDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.FeedbackRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.entity.Feedback;
import com.cognizant.agriserve.feedbackandsatisfactionservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.AdvisorySessionDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.FarmerDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.TrainingProgramDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.repo.FeedbackRepository;
import com.cognizant.agriserve.feedbackandsatisfactionservice.service.FeedbackService;
import com.cognizant.agriserve.feedbackandsatisfactionservice.util.FeedbackUtil;
import feign.FeignException; // <-- Added FeignException import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeedbcakServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired private AdvisorySessionClient advisorySessionClient;
    @Autowired private FarmerClient farmerClient;
    @Autowired private TrainingProgramClient trainingProgramClient;

    public FeedbackResponseDTO addFeedback(FeedbackRequestDTO dto){
        log.info("Recording farmer feedback for session and training program");

        // 1. Extract role in a single, final assignment
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String currentRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SERVICE";

        // 2. Validate and Fetch External Entities using Strict Exception Handling
        AdvisorySessionDTO ad = fetchAdvisorySessionSafely(dto.getSessionId());
        FarmerDTO fd = fetchFarmerSafely(dto.getFarmerId(), currentRole);
        TrainingProgramDTO dt = fetchTrainingProgramSafely(dto.getProgramId(), currentRole);

        // 3. Save to Local Database
        Feedback feedback = FeedbackUtil.tofeedback(dto);
        Feedback ft = feedbackRepository.save(feedback);

        return FeedbackResponseDTO.builder()
                .feedbackId(ft.getFeedbackId())
                .farmerName(fd.getName())
                .programName(dt.getTitle())
                .rating(ft.getRating())
                .comments(ft.getComments())
                .build();
    }


    public List<FeedbackResponseDTO> getAllFeedback(){
        log.info("Providing each and every record");

        // 1. Extract role in a single, final assignment
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String currentRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SERVICE";

        List<Feedback> feedbacks = feedbackRepository.findAll();

        Map<Long, String> farmerNames = new HashMap<>();
        Map<Long, String> programTitles = new HashMap<>();

        return feedbacks.stream().map(f -> {

            // Graceful Degradation: Matches your "enrichWithOfficerName" fallback pattern
            String fName = farmerNames.computeIfAbsent(f.getFarmerId(), id -> {
                try {
                    return farmerClient.getFarmer(id).getName();
                } catch (Exception e) {
                    log.warn("Could not fetch Farmer ID: {}. Service unavailable or missing.", id);
                    return "Unknown Farmer (Service Unavailable)";
                }
            });

            String pTitle = programTitles.computeIfAbsent(f.getTrainingProgramId(), id -> {
                try {
                    return trainingProgramClient.getProgramById(id).getTitle();
                } catch (Exception e) {
                    log.warn("Could not fetch Program ID: {}. Service unavailable or missing.", id);
                    return "Unknown Program (Service Unavailable)";
                }
            });

            return FeedbackResponseDTO.builder()
                    .feedbackId(f.getFeedbackId())
                    .rating(f.getRating())
                    .comments(f.getComments())
                    .farmerName(fName)
                    .programName(pTitle)
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // PRIVATE FEIGN VALIDATION HELPER METHODS (Based on Compliance Service)
    // =========================================================================

    private AdvisorySessionDTO fetchAdvisorySessionSafely(Long sessionId) {
        try {
            return advisorySessionClient.getAdvisoryById(sessionId);
        } catch (FeignException.NotFound e) {
            log.warn("Validation failed: Advisory Session ID {} does not exist.", sessionId);
            throw new ResourceNotFoundException("No Advisory Session found with ID: " + sessionId);
        } catch (FeignException e) {
            log.error("CRITICAL FEIGN ERROR: Status = {}, Body = {}", e.status(), e.contentUTF8());
            throw new RuntimeException("Failed to communicate with Advisory Service. Status: " + e.status());
        } catch (Exception e) {
            log.error("Could not reach Advisory Service", e);
            throw new RuntimeException("Advisory Service is currently unreachable.");
        }
    }

    private FarmerDTO fetchFarmerSafely(Long farmerId, String currentRole) {
        try {
            return farmerClient.getFarmer(farmerId);
        } catch (FeignException.NotFound e) {
            log.warn("Validation failed: Farmer ID {} does not exist.", farmerId);
            throw new ResourceNotFoundException("No Farmer found with ID: " + farmerId);
        } catch (FeignException e) {
            log.error("CRITICAL FEIGN ERROR: Status = {}, Body = {}", e.status(), e.contentUTF8());
            throw new RuntimeException("Failed to communicate with Farmer Service. Status: " + e.status());
        } catch (Exception e) {
            log.error("Could not reach Farmer Service", e);
            throw new RuntimeException("Farmer Service is currently unreachable.");
        }
    }

    private TrainingProgramDTO fetchTrainingProgramSafely(Long programId, String currentRole) {
        try {
            return trainingProgramClient.getProgramById(programId);
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