package com.cognizant.agriserve.farmerservice.controller;

import com.cognizant.agriserve.farmerservice.dto.request.FarmerUpdateRequestDTO;
import com.cognizant.agriserve.farmerservice.dto.request.RegisterFarmerDTO;
import com.cognizant.agriserve.farmerservice.dto.response.FarmerResponseDTO;
import com.cognizant.agriserve.farmerservice.service.FarmerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<FarmerResponseDTO> getMyProfile(
            @RequestHeader("X-Logged-In-User-Id") Long userId) {

        log.info("API Request: Fetching profile for User ID: {}", userId);
        FarmerResponseDTO profile = farmerService.getFarmerByUserId(userId);

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<FarmerResponseDTO> updateMyProfile(
            @RequestHeader("X-Logged-In-User-Id") Long userId,
            @Valid @RequestBody FarmerUpdateRequestDTO updateDto) {

        log.info("API Request: Updating profile for User ID: {}", userId);
        FarmerResponseDTO updatedProfile = farmerService.updateFarmerProfile(userId, updateDto);

        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ExtensionOfficer', 'Admin')")
    public ResponseEntity<List<FarmerResponseDTO>> getAllFarmers() {

        log.info("API Request: Fetching all registered farmers for administrative review");
        List<FarmerResponseDTO> farmers = farmerService.getAllFarmers();

        return ResponseEntity.ok(farmers);
    }

    @GetMapping("/{farmerId}")
    @PreAuthorize("hasRole('ExtensionOfficer', 'Admin')")
    public ResponseEntity<FarmerResponseDTO> getFarmerById(@PathVariable Long farmerId) {
        log.info("API Request: Fetching profile for Farmer ID: {}", farmerId);
        return ResponseEntity.ok(farmerService.getFarmerById(farmerId));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<Void> createFarmerProfile(@RequestBody RegisterFarmerDTO registerrequestdto){
      log.info("API request: Data Coming from Authservice:");
      return ResponseEntity.ok(farmerService.registerfarmer(registerrequestdto));
    }
}