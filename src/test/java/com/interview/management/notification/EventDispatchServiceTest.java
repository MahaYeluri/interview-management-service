package com.interview.management.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.management.enums.EventType;
import com.interview.management.enums.RecipientType;
import com.interview.management.models.Candidate;
import com.interview.management.models.EventDispatch;
import com.interview.management.models.Interview;
import com.interview.management.models.Interviewer;
import com.interview.management.repositories.EventDispatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDispatchServiceTest {

    @Mock
    private EventDispatchRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventDispatchService service;

    private Candidate candidate;
    private Interviewer interviewer;
    private Interview interview;

    @BeforeEach
    void setUp() {

        candidate = mock(Candidate.class);
        when(candidate.getName()).thenReturn("John Candidate");
        when(candidate.getEmail()).thenReturn("john@test.com");

        interviewer = mock(Interviewer.class);
        when(interviewer.getName()).thenReturn("Jane Interviewer");
        when(interviewer.getEmail()).thenReturn("jane@test.com");

        interview = mock(Interview.class);

        when(interview.getId()).thenReturn(100L);
        when(interview.getCandidate()).thenReturn(candidate);
        when(interview.getInterviewers())
                .thenReturn(new HashSet<>(Set.of(interviewer)));

        when(interview.getRound()).thenReturn(null);
        when(interview.getStartTime())
                .thenReturn(LocalDateTime.of(
                        2026, 10, 5, 10, 0));

        when(interview.getEndTime())
                .thenReturn(LocalDateTime.of(
                        2026, 10, 5, 11, 0));

        when(interview.getMeetingLink())
                .thenReturn("https://meeting.test");
    }

    @Test
    void enqueueInterviewScheduled_shouldCreatePendingEvent()
            throws Exception {

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"eventType\":\"INTERVIEW_SCHEDULED\"}");

        service.enqueueInterviewScheduled(interview);

        ArgumentCaptor<EventDispatch> captor =
                ArgumentCaptor.forClass(EventDispatch.class);

        verify(repository).save(captor.capture());

        EventDispatch event = captor.getValue();

        assertNotNull(event);

        verify(objectMapper)
                .writeValueAsString(any());
    }

    @Test
    void enqueueInterviewScheduled_shouldFailWhenPayloadCannotBeSerialized()
            throws Exception {

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(
                        new com.fasterxml.jackson.core.JsonProcessingException(
                                "Serialization failed") {
                        }
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.enqueueInterviewScheduled(interview)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Could not serialize notification payload")
        );

        verify(repository, never())
                .save(any());
    }
}


