package com.cognizant.agriserve.complianceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ADVISORYSERVICE")
public interface AdvisoryClient {

    @GetMapping("/api/advisory-sessions/{sessionId}/exists")
    void verifyAdvisorySessionExists(@PathVariable("sessionId") Long sessionId);

}