package com.cognizant.agriserve.complianceservice.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT) // Prevents ambiguous mapping bugs
                .setFieldMatchingEnabled(true)                  // Enables private field mapping
                .setSkipNullEnabled(true);                      // Don't overwrite existing data with nulls

        return mapper;
    }
}