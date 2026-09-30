package com.interview.management.dto;

import com.interview.management.enums.InterviewMode;
import com.interview.management.enums.InterviewRound;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

import java.util.Set;

public record ScheduleInterviewRequest( @NotNull Long candidateId,
                                        @NotEmpty Set<Long> interviewerIds,
                                        @NotNull @Future LocalDateTime startTime,
                                        @Min(15) @Max(240) int durationMinutes,
                                        @NotNull InterviewRound round,
                                        @NotNull InterviewMode mode,
                                        String meetingLink) {
}
