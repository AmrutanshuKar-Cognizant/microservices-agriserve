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
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'Admin')")
    public ResponseEntity<List<FarmerResponseDTO>> getAllFarmers(@RequestHeader("X-User-Role") String role) {

        log.info("API Request: Fetching all registered farmers for administrative review");
        List<FarmerResponseDTO> farmers = farmerService.getAllFarmers(role);

        return ResponseEntity.ok(farmers);
    }

    @GetMapping("/{farmerId}")
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'Admin', 'SERVICE')")
    public ResponseEntity<FarmerResponseDTO> getFarmerById(@PathVariable Long farmerId,
                                                           @RequestHeader("X-User-Role") String role) {
        log.info("API Request: Fetching profile for Farmer ID: {}", farmerId);
        return ResponseEntity.ok(farmerService.getFarmerById(farmerId, role));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('SERVICE')") // Fixes the 401!
    public ResponseEntity<Void> createFarmerProfile(@RequestBody RegisterFarmerDTO registerrequestdto){
        log.info("API request: Data Coming from Authservice");
        farmerService.registerfarmer(registerrequestdto); // Call the service
        return ResponseEntity.ok().build(); // Return a 200 OK with an empty body
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<FarmerResponseDTO> getFarmerByUserId(@PathVariable Long userId) {
        log.info("API Request: Service-to-Service call fetching Farmer by User ID: {}", userId);

        // Calls the existing logic in FarmerServiceImpl
        FarmerResponseDTO farmerProfile = farmerService.getFarmerByUserId(userId);

        return ResponseEntity.ok(farmerProfile);

    }

    @GetMapping("/feigncall/{id}")
    @PreAuthorize("hasRole('SERVICE')")
    public FarmerResponseDTO getFarmerForFeign(@PathVariable("id") Long id) {
        log.info("Internal Feign call received for Farmer ID: {}", id);
        // Note: Returning the DTO directly so Feign can deserialize it seamlessly
        return farmerService.getFarmerForFeign(id);
    }
}