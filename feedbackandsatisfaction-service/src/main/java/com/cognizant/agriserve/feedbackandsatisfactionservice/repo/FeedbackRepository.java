package com.cognizant.agriserve.feedbackandsatisfactionservice.repo;

import com.cognizant.agriserve.feedbackandsatisfactionservice.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback,Long> {
    List<Feedback> findByTrainingProgramId(Long programId);
}
