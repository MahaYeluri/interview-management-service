package com.interview.management.dto;

import com.interview.management.enums.InterviewMode;
import com.interview.management.enums.InterviewRound;
import com.interview.management.enums.InterviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewResponse(Long id, Long candidateId, String candidateName,
                                List<String> interviewerNames,
                                LocalDateTime startTime, LocalDateTime endTime,
                                InterviewRound round, InterviewMode mode, InterviewStatus status) {
}
