package com.cognizant.agriserve.farmerservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class RegisterFarmerDTO {
    private String name;
    private String dob;
    private String gender;
    private String address;
    private String contactInfo;
    private Double landSize;
    private String cropType;
    private Long userId;
}
