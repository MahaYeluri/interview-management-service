package com.interview.management.dto;

import com.interview.management.enums.Recommendation;

import java.time.LocalDateTime;

public record FeedbackResponse(Long id,
                               Integer rating,
                               String comments,
                               Recommendation recommendation,
                               LocalDateTime submittedAt) {
}
