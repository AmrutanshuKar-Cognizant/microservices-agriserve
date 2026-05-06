package com.cognizant.agriserve.farmerservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmerDocument") // Standard plural naming
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerDocument {
    public enum VerificationStatus{
        PENDING,
        VERIFIED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    @NotNull(message = "Farmer association is required")
    private Farmer farmer;

    @NotBlank(message = "Document type is mandatory")
    @Column(nullable = false)
    private String docType; // e.g., Aadhar, Land Permit, etc.

    @NotBlank(message = "File URI cannot be empty")
    @Column(nullable = false, unique = true)
    private String fileURI;

    @NotNull(message = "Upload date is required")
    @PastOrPresent(message = "Upload date cannot be in the future")
    private LocalDate uploadedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

