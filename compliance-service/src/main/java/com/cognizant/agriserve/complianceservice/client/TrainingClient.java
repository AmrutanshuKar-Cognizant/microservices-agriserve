package com.cognizant.agriserve.complianceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TRAININGSERVICE")
public interface TrainingClient {

    @GetMapping("/api/programs/{programId}/exists")
    void checkProgramExists(@PathVariable("programId") Long programId);

}