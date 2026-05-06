package com.cognizant.agriserve.feedbackandsatisfactionservice.util;

import com.cognizant.agriserve.feedbackandsatisfactionservice.dto.FeedbackRequestDTO;
import com.cognizant.agriserve.feedbackandsatisfactionservice.entity.Feedback;

public class FeedbackUtil {

public static  Feedback tofeedback(FeedbackRequestDTO dto){
    Feedback ft=new Feedback();
    ft.setFarmerId(dto.getFarmerId());
    ft.setSessionId(dto.getSessionId());
    ft.setTrainingProgramId(dto.getProgramId());
    ft.setRating(dto.getRating());
    ft.setComments(dto.getComment());
    ft.setDate(dto.getDate());
    return ft;
}

}
