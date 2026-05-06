package com.cognizant.agriserve.feedbackandsatisfactionservice.service;

import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.SatisfactionMetricResponseDTO;

import java.util.List;

public interface SatisfactionMetricService {
    SatisfactionMetricResponseDTO evaluate(SatisfactionMetricRequestDTO dto);

    List<SatisfactionMetricResponseDTO> getSatisfactionmetric();
}
