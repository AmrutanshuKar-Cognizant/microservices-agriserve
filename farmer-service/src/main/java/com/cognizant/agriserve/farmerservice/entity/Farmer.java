package com.cognizant.agriserve.farmerservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farmer {
    public enum Status {
        PENDING,
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long farmerId;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Date of birth is required")
    private String dob;

    @Column(nullable = false)
    private String gender;

    @NotBlank(message = "Address is required")
    @Column(columnDefinition = "TEXT")
    private String address;

    @NotBlank(message = "Contact info is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Column(unique = true, nullable = false)
    private String contactInfo;

    @PositiveOrZero(message = "Land size cannot be negative")
    private Double landSize;

    private String cropType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name="user_id", nullable = false, unique = true)
    private Long userId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}