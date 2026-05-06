package com.cognizant.agriserve.feedbackandsatisfactionservice.controller;

import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.FeedbackRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.FeedbackResponseDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {


    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(@RequestBody @Valid FeedbackRequestDTO dto) {
        log.info("REST request to submit feedback for Farmer ID: {}", dto.getFarmerId());
        return ResponseEntity.ok(feedbackService.addFeedback(dto));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager')")
    public List<FeedbackResponseDTO> getAll() {
        log.info("REST request to fetch all feedback records");
        return feedbackService.getAllFeedback();
    }
}
