package com.cognizant.agriserve.trainingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopRequestDTO {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotNull(message = "Officer ID is required")
    private Long officerId;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    @NotNull(message = "Workshop date and time are required")
    private LocalDateTime date;

}
