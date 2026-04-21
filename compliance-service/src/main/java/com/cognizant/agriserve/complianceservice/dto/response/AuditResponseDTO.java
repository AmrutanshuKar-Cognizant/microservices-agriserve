package com.cognizant.agriserve.complianceservice.dto.response;

import com.cognizant.agriserve.complianceservice.entity.Audit.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponseDTO {

    private Long auditId;

    private Long officerId;

    private String officerName;

    private String scope;

    private String findings;

    private LocalDateTime date;

    private AuditStatus status;

}