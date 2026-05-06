package com.cognizant.agriserve.feedbackandsatisfactionservice.client;

import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.AdvisorySessionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ADVISORY-SERVICE")
public interface AdvisorySessionClient {

    @GetMapping("/api/advisory-sessions/{id}")
    AdvisorySessionDTO getAdvisoryById(@PathVariable("id") Long id);
}
