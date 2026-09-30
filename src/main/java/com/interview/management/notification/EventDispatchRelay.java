package com.interview.management.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.management.dto.NotificationRequest;
import com.interview.management.enums.DispatchStatus;
import com.interview.management.models.EventDispatch;
import com.interview.management.repositories.EventDispatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventDispatchRelay {

    private static final int MAX_ATTEMPTS = 5;

    private final EventDispatchRepository repository;
    private final NotificationClient notificationClient;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${dispatch.poll-interval-ms:5000}")
    public void dispatchPending() {
        repository.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        DispatchStatus.PENDING, LocalDateTime.now())
                .forEach(this::dispatch);
    }

    // Deliberately not @Transactional: no DB transaction stays open during the HTTP call
    private void dispatch(EventDispatch event) {
        try {
            NotificationRequest request = objectMapper.readValue(event.getPayload(), NotificationRequest.class);
            notificationClient.send(request);
            event.markDispatched();
            log.info("Dispatched eventId={}", event.getEventId());
        } catch (JsonProcessingException | HttpClientErrorException ex) {
            event.markPermanentlyFailed(ex.getMessage());              // 4xx or bad payload will never succeed
            log.error("Event {} permanently failed", event.getEventId(), ex);
        } catch (RestClientException ex) {
            event.markAttemptFailed(ex.getMessage(), MAX_ATTEMPTS);    // timeout / 5xx → retry with backoff
            log.warn("Event {} attempt {} failed: {}", event.getEventId(), event.getAttempts(), ex.getMessage());
        }
        repository.save(event);
    }
}