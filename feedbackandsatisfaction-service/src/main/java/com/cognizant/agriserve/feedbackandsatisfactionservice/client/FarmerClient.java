package com.cognizant.agriserve.feedbackandsatisfactionservice.client;

import com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto.FarmerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="FARMER-SERVICE")
public interface FarmerClient {

    @GetMapping("/api/farmers/feigncall/{id}")
    FarmerDTO getFarmer(@PathVariable Long id);
}
