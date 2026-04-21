package com.cognizant.agriserve.complianceservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecord {

    public enum ComplianceType {
        ADVISORY,
        TRAINING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_type", nullable = false)
    private ComplianceType type;

    @Column(nullable = false)
    private String result;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            // UTC
            this.date = LocalDateTime.now();
        }
    }
}