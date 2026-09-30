package com.interview.management.services;


import com.interview.management.dto.InterviewResponse;
import com.interview.management.dto.ScheduleInterviewRequest;
import com.interview.management.dto.SubmitFeedbackRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterviewService {

    InterviewResponse scheduleInterview(ScheduleInterviewRequest request);
    InterviewResponse submitFeedback(Long interviewId, SubmitFeedbackRequest request);
    Page<InterviewResponse> searchInterviews(String candidateName, String interviewerName, Pageable pageable);
}
