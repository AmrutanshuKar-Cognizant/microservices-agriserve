package com.cognizant.agriserve.farmerservice.service.impl;

import com.cognizant.agriserve.farmerservice.dao.FarmerRepository;
import com.cognizant.agriserve.farmerservice.dto.request.FarmerUpdateRequestDTO;
import com.cognizant.agriserve.farmerservice.dto.request.RegisterFarmerDTO;
import com.cognizant.agriserve.farmerservice.dto.response.FarmerResponseDTO;
import com.cognizant.agriserve.farmerservice.entity.Farmer;
import com.cognizant.agriserve.farmerservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.farmerservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.farmerservice.service.FarmerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;
    private final ModelMapper modelMapper;

    @Override
    public FarmerResponseDTO getFarmerByUserId(Long userId) {
        log.debug("Fetching farmer profile for User ID: {}", userId);

        // Locating the profile created by Auth-Service using the logical userId link
        Farmer farmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found for User ID: " + userId));

        return mapToResponse(farmer);
    }

    @Override
    @Transactional
    public FarmerResponseDTO updateFarmerProfile(Long userId, FarmerUpdateRequestDTO updateDto) {
        log.info("Updating farmer profile details for User ID: {}", userId);

        Farmer existingFarmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found for User ID: " + userId));

        // 1. Map basic fields (Name, Address, Contact, CropType, LandSize)
        modelMapper.map(updateDto, existingFarmer);

        // 3. Save updated entity
        Farmer updatedFarmer = farmerRepository.save(existingFarmer);
        log.info("Successfully updated profile for Farmer ID: {}", updatedFarmer.getFarmerId());

        return mapToResponse(updatedFarmer);
    }

    @Override
    public List<FarmerResponseDTO> getAllFarmers(String role) {
        log.debug("Administrative fetch: Retrieving all farmer profiles");

        // 1. Check Role Authorization
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("EXTENSIONOFFICER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Extension Officers can view the full farmer list.");
        }

        return farmerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FarmerResponseDTO getFarmerById(Long farmerId, String role) {
        log.debug("Fetching profile by internal Farmer ID: {}", farmerId);

        // 1. Check Role Authorization
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("EXTENSIONOFFICER"))) {
            throw new UnauthorizedActionException("Access Denied: You do not have permission to view specific farmer profiles.");
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));
        return mapToResponse(farmer);
    }

    @Override
    public Void registerfarmer(RegisterFarmerDTO registerrequestdto) {
        log.info("Register farmer data into its table");

        Farmer farmer=new Farmer();
        farmer.setName(registerrequestdto.getName());
        farmer.setDob(registerrequestdto.getDob());
        farmer.setAddress(registerrequestdto.getAddress());
        farmer.setCropType(registerrequestdto.getCropType());
        farmer.setGender(registerrequestdto.getGender());
        farmer.setContactInfo(registerrequestdto.getContactInfo());
        farmer.setLandSize(registerrequestdto.getLandSize());
        farmer.setUserId(registerrequestdto.getUserId());
        farmerRepository.save(farmer);
        return null;
    }


    private FarmerResponseDTO mapToResponse(Farmer farmer) {
        return modelMapper.map(farmer, FarmerResponseDTO.class);
    }
}