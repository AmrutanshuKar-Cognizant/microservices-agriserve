package com.cognizant.agriserve.complianceservice.dto.response;

import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord.ComplianceType; // Updated import
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecordResponseDTO {

    private Long complianceId;

    private Long entityId;

//    private String entityName;

    private ComplianceType type;

    private Long officerId;

    private String officerName;

    private String result;

    private LocalDateTime date;

    private String notes;

}