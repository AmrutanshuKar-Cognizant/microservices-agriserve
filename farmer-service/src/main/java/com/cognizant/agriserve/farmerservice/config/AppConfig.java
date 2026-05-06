package com.cognizant.agriserve.farmerservice.config;

import com.cognizant.agriserve.farmerservice.dto.response.FarmerDocumentResponseDTO;
import com.cognizant.agriserve.farmerservice.entity.FarmerDocument;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 1. Tell ModelMapper to only map fields if the names match exactly
        // This prevents it from trying to guess that 'getDocumentId' should map to 'farmerId'
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // 2. Explicitly define the mapping for the troublesome field
        modelMapper.typeMap(FarmerDocument.class, FarmerDocumentResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getFarmer().getFarmerId(), FarmerDocumentResponseDTO::setFarmerId);
                });

        return modelMapper;
    }
}