package com.cognizant.agriserve.farmerservice.dto.response;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerResponseDTO {

    private Long farmerId;

    private String name;

    private String dob;

    private String gender;

    private String address;

    private String contactInfo;

    private Double landSize;

    private String cropType;

    private String status;

    private Long userId;
}