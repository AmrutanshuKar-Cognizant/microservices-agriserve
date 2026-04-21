package com.cognizant.agriserve.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    // For User Service

    private String name;
    private String email;
    private String contactInfo;
    private String password;
    private String role;

    // The Crucial Link
    private Long userId;

    // For Farmer Service
    private String dob;
    private String gender;
    private String address;
    private Double landSize;
    private String cropType;
}