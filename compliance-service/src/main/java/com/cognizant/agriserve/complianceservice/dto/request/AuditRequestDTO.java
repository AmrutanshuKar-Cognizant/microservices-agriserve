package com.cognizant.agriserve.complianceservice.dto.request;

import com.cognizant.agriserve.complianceservice.entity.Audit.AuditStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequestDTO {

    @NotBlank(message = "Scope cannot be blank or empty")
    @Size(min = 5, max = 100, message = "Scope must be between 5 and 100 characters")
    private String scope;

    @NotBlank(message = "Findings are required")
    private String findings;

    @NotNull(message = "Audit status is required")
    private AuditStatus status;
}