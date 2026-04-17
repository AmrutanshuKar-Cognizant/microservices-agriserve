package com.cognizant.agriserve.authservice.client;

import com.cognizant.agriserve.authservice.dto.RegisterRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "FARMERSERVICE")
public interface FarmerClient {

    @PostMapping("/api/farmers/register")
    void registerFarmer(@RequestBody RegisterRequestDTO registerRequestDTO);
}