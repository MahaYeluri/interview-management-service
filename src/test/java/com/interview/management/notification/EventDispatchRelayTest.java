package com.interview.management.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.management.dto.NotificationRequest;
import com.interview.management.enums.DispatchStatus;
import com.interview.management.models.EventDispatch;
import com.interview.management.repositories.EventDispatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDispatchRelayTest {

    @Mock
    private EventDispatchRepository repository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventDispatch event;

    @Mock
    private NotificationRequest notificationRequest;

    @InjectMocks
    private EventDispatchRelay relay;

    @BeforeEach
    void setUp() {
        when(event.getPayload())
                .thenReturn("{\"eventType\":\"INTERVIEW_SCHEDULED\"}");

        when(event.getEventId())
                .thenReturn("event-123");
    }

    @Test
    void dispatchPending_shouldDispatchSuccessfully() throws Exception {

        when(repository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        eq(DispatchStatus.PENDING),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        when(objectMapper.readValue(
                anyString(),
                eq(NotificationRequest.class)))
                .thenReturn(notificationRequest);

        doNothing()
                .when(notificationClient)
                .send(notificationRequest);

        relay.dispatchPending();

        verify(objectMapper)
                .readValue(
                        anyString(),
                        eq(NotificationRequest.class)
                );

        verify(notificationClient)
                .send(notificationRequest);

        verify(event)
                .markDispatched();

        verify(repository)
                .save(event);

        verify(event, never())
                .markPermanentlyFailed(anyString());

        verify(event, never())
                .markAttemptFailed(anyString(), anyInt());
    }

    @Test
    void dispatchPending_shouldPermanentlyFailForInvalidJson()
            throws Exception {

        when(repository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        eq(DispatchStatus.PENDING),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        when(objectMapper.readValue(
                anyString(),
                eq(NotificationRequest.class)))
                .thenThrow(
                        new JsonProcessingException("Invalid JSON") {
                        }
                );

        relay.dispatchPending();

        verify(event)
                .markPermanentlyFailed(
                        contains("Invalid JSON")
                );

        verify(notificationClient, never())
                .send(any());

        verify(event, never())
                .markDispatched();

        verify(event, never())
                .markAttemptFailed(anyString(), anyInt());

        verify(repository)
                .save(event);
    }

    @Test
    void dispatchPending_shouldPermanentlyFailForHttp4xx() throws JsonProcessingException {

        when(repository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        eq(DispatchStatus.PENDING),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        when(objectMapper.readValue(
                anyString(),
                eq(NotificationRequest.class)))
                .thenReturn(notificationRequest);

        HttpClientErrorException exception =
                HttpClientErrorException.create(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                );

        doThrow(exception)
                .when(notificationClient)
                .send(notificationRequest);

        relay.dispatchPending();

        verify(event)
                .markPermanentlyFailed(
                        contains("400")
                );

        verify(event, never())
                .markDispatched();

        verify(event, never())
                .markAttemptFailed(anyString(), anyInt());

        verify(repository)
                .save(event);
    }

    @Test
    void dispatchPending_shouldRetryForRestClientException() {

        when(repository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        eq(DispatchStatus.PENDING),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        try {
            when(objectMapper.readValue(
                    anyString(),
                    eq(NotificationRequest.class)))
                    .thenReturn(notificationRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        doThrow(new RestClientException("Notification service unavailable"))
                .when(notificationClient)
                .send(notificationRequest);

        relay.dispatchPending();

        verify(event)
                .markAttemptFailed(
                        contains("Notification service unavailable"),
                        eq(5)
                );

        verify(event, never())
                .markDispatched();

        verify(event, never())
                .markPermanentlyFailed(anyString());

        verify(repository)
                .save(event);
    }


}

