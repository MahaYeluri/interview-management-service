package com.interview.management.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.management.dto.NotificationRequest;
import com.interview.management.enums.EventType;

import com.interview.management.enums.RecipientType;
import com.interview.management.models.EventDispatch;
import com.interview.management.models.Interview;
import com.interview.management.repositories.EventDispatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
public class EventDispatchService {

    private final EventDispatchRepository repository;
    private final ObjectMapper objectMapper;

    // MANDATORY: must be called inside the interview's transaction, so both rows commit or roll back together
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueueInterviewScheduled(Interview interview) {
        UUID eventId = UUID.randomUUID();

        List<NotificationRequest.Recipient> recipients = new ArrayList<>();
        recipients.add(new NotificationRequest.Recipient(
                RecipientType.CANDIDATE, interview.getCandidate().getName(), interview.getCandidate().getEmail()));
        interview.getInterviewers().forEach(i -> recipients.add(new NotificationRequest.Recipient(
                RecipientType.INTERVIEWER, i.getName(), i.getEmail())));

        Map<String, String> details = Map.of(
                "candidateName", String.valueOf(interview.getCandidate().getName()),
                "round", String.valueOf(interview.getRound()),
                "startTime", String.valueOf(interview.getStartTime()),
                "endTime", String.valueOf(interview.getEndTime()),
                "meetingLink", Objects.toString(interview.getMeetingLink(), "N/A"));

        NotificationRequest payload = new NotificationRequest(
                eventId, EventType.INTERVIEW_SCHEDULED, interview.getId(), recipients, details);

        repository.save(EventDispatch.pending(eventId.toString(), "INTERVIEW", interview.getId(),
                EventType.INTERVIEW_SCHEDULED, toJson(payload)));
    }

    private String toJson(NotificationRequest payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize notification payload", e);
        }
    }
}