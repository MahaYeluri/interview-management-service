package com.interview.management.controller;



import com.interview.management.controllers.InterviewController;
import com.interview.management.dto.InterviewResponse;
import com.interview.management.dto.ScheduleInterviewRequest;
import com.interview.management.dto.SubmitFeedbackRequest;
import com.interview.management.services.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private InterviewResponse response;

    @BeforeEach
    void setUp() {
        response = mock(InterviewResponse.class);
    }

    @Test
    void scheduleInterview_shouldReturnCreated() {

        ScheduleInterviewRequest request =
                mock(ScheduleInterviewRequest.class);

        when(interviewService.scheduleInterview(request))
                .thenReturn(response);

        ResponseEntity<InterviewResponse> result =
                interviewController.scheduleInterview(request);

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode()
        );

        assertSame(response, result.getBody());

        verify(interviewService)
                .scheduleInterview(request);
    }

    @Test
    void submitFeedback_shouldReturnOk() {

        Long interviewId = 100L;

        SubmitFeedbackRequest request =
                mock(SubmitFeedbackRequest.class);

        when(interviewService.submitFeedback(
                interviewId,
                request))
                .thenReturn(response);

        ResponseEntity<InterviewResponse> result =
                interviewController.submitFeedback(
                        interviewId,
                        request
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(response, result.getBody());

        verify(interviewService)
                .submitFeedback(interviewId, request);
    }

    @Test
    void searchInterviews_shouldReturnOk() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<InterviewResponse> page =
                new PageImpl<>(List.of(response), pageable, 1);

        when(interviewService.searchInterviews(
                "John",
                "Jane",
                pageable))
                .thenReturn(page);

        ResponseEntity<Page<InterviewResponse>> result =
                interviewController.searchInterviews(
                        "John",
                        "Jane",
                        pageable
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(page, result.getBody());

        verify(interviewService)
                .searchInterviews(
                        "John",
                        "Jane",
                        pageable
                );
    }

    @Test
    void searchInterviews_shouldAllowNullFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<InterviewResponse> page =
                new PageImpl<>(List.of(), pageable, 0);

        when(interviewService.searchInterviews(
                null,
                null,
                pageable))
                .thenReturn(page);

        ResponseEntity<Page<InterviewResponse>> result =
                interviewController.searchInterviews(
                        null,
                        null,
                        pageable
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());

        verify(interviewService)
                .searchInterviews(
                        null,
                        null,
                        pageable
                );
    }
}


