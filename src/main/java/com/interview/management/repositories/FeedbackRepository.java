package com.interview.management.repositories;

import com.interview.management.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback,Long> {
    boolean existsByInterview_IdAndInterviewer_Id(Long interviewId, Long interviewerId);

}
