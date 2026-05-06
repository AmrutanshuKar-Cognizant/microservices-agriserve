package com.cognizant.agriserve.userservice.client;

import com.cognizant.agriserve.userservice.dto.RegisterFarmerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="FARMER-SERVICE")
public interface FarmerClient {

    @PostMapping("/api/farmers/register")
    void createFarmerProfile(@RequestBody RegisterFarmerDTO registerFarmerDTO);
}
