package com.interview.management.dto;

import com.interview.management.enums.EventType;
import com.interview.management.enums.RecipientType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NotificationRequest(
        @NotNull UUID eventId,
        @NotNull EventType eventType,
        @NotNull Long interviewId,
        @NotEmpty List<@Valid Recipient> recipients,
        @NotNull Map<String, String> details) {

    public record Recipient(
            @NotNull RecipientType type,
            String name,
            @NotBlank @Email String email) {}
}