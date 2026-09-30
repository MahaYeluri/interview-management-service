package com.interview.management.dto;

import com.interview.management.enums.Recommendation;
import jakarta.validation.constraints.*;

public record SubmitFeedbackRequest(   @NotNull Long interviewerId,
                                       @Min(1) @Max(5) int rating,
                                       @NotBlank @Size(max = 2000) String comments,
                                       @NotNull Recommendation recommendation) {
}
