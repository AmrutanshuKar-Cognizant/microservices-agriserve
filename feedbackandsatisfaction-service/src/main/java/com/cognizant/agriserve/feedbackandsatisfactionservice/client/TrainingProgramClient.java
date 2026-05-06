package com.cognizant.agriserve.feedbackandsatisfactionservice.client;

import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.TrainingProgramDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="TRAINING-SERVICE")
public interface TrainingProgramClient {

    @GetMapping("/api/programs/feigncall/{id}")
    TrainingProgramDTO getProgramById(@PathVariable("id") Long id);}
