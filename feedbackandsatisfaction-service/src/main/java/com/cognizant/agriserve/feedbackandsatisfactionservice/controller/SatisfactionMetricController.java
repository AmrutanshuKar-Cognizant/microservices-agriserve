package com.cognizant.agriserve.feedbackandsatisfactionservice.controller;

import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricResponseDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.service.SatisfactionMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/satisfaction-metrics")
@RequiredArgsConstructor
public class SatisfactionMetricController {

    private final SatisfactionMetricService metricservice;

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager')")
    public ResponseEntity<SatisfactionMetricResponseDTO> evaluateProgram(@RequestBody @Valid SatisfactionMetricRequestDTO dto) {
        log.info("Received request to evaluate Program ID: {}", dto.getProgramId());
        return ResponseEntity.ok(metricservice.evaluate(dto));
    }
}
