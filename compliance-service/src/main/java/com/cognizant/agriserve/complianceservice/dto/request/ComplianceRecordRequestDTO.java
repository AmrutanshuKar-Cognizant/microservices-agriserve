package com.cognizant.agriserve.complianceservice.dto.request;

import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord.ComplianceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplianceRecordRequestDTO {


    @NotNull(message = "Entity ID is required")
    @Positive(message = "Entity ID must be a valid positive number")
    private Long entityId;

    @NotNull(message = "Compliance type is required")
    private ComplianceType type;

    @NotBlank(message = "Result cannot be blank")
    private String result;

    @NotBlank(message = "Notes are required")
    @Size(max = 500)
    private String notes;
}