package com.cognizant.agriserve.feedbackandsatisfactionservice.externaldto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvisorySessionDTO {
    private Long sessionId;
    private String contentTitle;
}
