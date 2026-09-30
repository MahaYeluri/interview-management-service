package com.interview.management.service;


import com.interview.management.dto.InterviewResponse;
import com.interview.management.dto.ScheduleInterviewRequest;
import com.interview.management.dto.SubmitFeedbackRequest;
import com.interview.management.enums.InterviewMode;
import com.interview.management.enums.InterviewRound;
import com.interview.management.enums.InterviewStatus;
import com.interview.management.enums.Recommendation;
import com.interview.management.exception.*;
import com.interview.management.models.Candidate;
import com.interview.management.models.Feedback;
import com.interview.management.models.Interview;
import com.interview.management.models.Interviewer;
import com.interview.management.models.InterviewerAvailability;
import com.interview.management.notification.EventDispatchService;
import com.interview.management.repositories.CandidateRepository;
import com.interview.management.repositories.FeedbackRepository;
import com.interview.management.repositories.InterviewRepository;
import com.interview.management.repositories.InterviewerAvailabilityRepository;
import com.interview.management.repositories.InterviewerRepository;

import com.interview.management.services.InterviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceImplTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private InterviewerRepository interviewerRepository;

    @Mock
    private InterviewerAvailabilityRepository availabilityRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private EventDispatchService eventDispatchService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private Candidate candidate;
    private Interviewer interviewer;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    @BeforeEach
    void setUp() {

        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setName("John Candidate");

        interviewer = new Interviewer();
        interviewer.setId(10L);
        interviewer.setName("Jane Interviewer");

        startTime = LocalDateTime.of(2026, 10, 5, 10, 0);
        endTime = startTime.plusMinutes(60);
    }

    // ============================================================
    // Schedule Interview - SUCCESS
    // ============================================================

    @Test
    void scheduleInterview_shouldScheduleSuccessfully() {

        ScheduleInterviewRequest request =
                new ScheduleInterviewRequest(
                        1L,
                        Set.of(10L),
                        startTime,
                        60,
                        InterviewRound.SCREENING,
                        InterviewMode.VIRTUAL,
                        "https://meeting.test"
                );

        InterviewerAvailability availability =
                mockAvailability(interviewer, 9, 0, 18, 0);

        when(availabilityRepository
                .findByInterviewerIdInAndDayOfWeek(
                        eq(Set.of(10L)),
                        eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of(availability));

        when(interviewRepository.existsConflict(
                eq(Set.of(10L)),
                eq(startTime),
                eq(endTime)))
                .thenReturn(false);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(interviewerRepository.findAllById(Set.of(10L)))
                .thenReturn(List.of(interviewer));

        when(interviewRepository.save(any(Interview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterviewResponse response =
                interviewService.scheduleInterview(request);

        assertNotNull(response);

        ArgumentCaptor<Interview> captor =
                ArgumentCaptor.forClass(Interview.class);

        verify(interviewRepository).save(captor.capture());

        Interview savedInterview = captor.getValue();

        assertSame(candidate, savedInterview.getCandidate());

        assertEquals(
                Set.of(interviewer),
                savedInterview.getInterviewers()
        );

        assertEquals(startTime, savedInterview.getStartTime());
        assertEquals(endTime, savedInterview.getEndTime());

        assertEquals(
                "https://meeting.test",
                savedInterview.getMeetingLink()
        );

        assertEquals(
                InterviewStatus.SCHEDULED,
                savedInterview.getStatus()
        );

        assertEquals(
                InterviewMode.VIRTUAL,
                savedInterview.getMode()
        );

        assertEquals(
                InterviewRound.SCREENING,
                savedInterview.getRound()
        );

        verify(eventDispatchService)
                .enqueueInterviewScheduled(savedInterview);
    }


    // ============================================================
    // Schedule Interview - booking conflict
    // ============================================================

    @Test
    void scheduleInterview_shouldThrowWhenBookingConflictExists() {

        ScheduleInterviewRequest request =
                new ScheduleInterviewRequest(
                        1L,
                        Set.of(10L),
                        startTime,
                        60,
                        InterviewRound.SCREENING,
                        InterviewMode.VIRTUAL,
                        null
                );

        InterviewerAvailability availability =
                mockAvailability(interviewer, 9, 0, 18, 0);

        when(availabilityRepository
                .findByInterviewerIdInAndDayOfWeek(
                        eq(Set.of(10L)),
                        eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of(availability));

        when(interviewRepository.existsConflict(
                eq(Set.of(10L)),
                eq(startTime),
                eq(endTime)))
                .thenReturn(true);

        assertThrows(
                SchedulingConflictException.class,
                () -> interviewService.scheduleInterview(request)
        );

        verify(candidateRepository, never())
                .findById(anyLong());

        verify(interviewerRepository, never())
                .findAllById(anySet());

        verify(interviewRepository, never())
                .save(any());

        verify(eventDispatchService, never())
                .enqueueInterviewScheduled(any());
    }

    // ============================================================
    // Schedule Interview - candidate not found
    // ============================================================

    @Test
    void scheduleInterview_shouldThrowWhenCandidateNotFound() {

        ScheduleInterviewRequest request =
                new ScheduleInterviewRequest(
                        999L,
                        Set.of(10L),
                        startTime,
                        60,
                        InterviewRound.SCREENING,
                        InterviewMode.VIRTUAL,
                        null
                );

        InterviewerAvailability availability =
                mockAvailability(interviewer, 9, 0, 18, 0);

        when(availabilityRepository
                .findByInterviewerIdInAndDayOfWeek(
                        eq(Set.of(10L)),
                        eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of(availability));

        when(interviewRepository.existsConflict(
                eq(Set.of(10L)),
                eq(startTime),
                eq(endTime)))
                .thenReturn(false);

        when(candidateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService.scheduleInterview(request)
        );

        verify(interviewerRepository, never())
                .findAllById(anySet());

        verify(interviewRepository, never())
                .save(any());

        verify(eventDispatchService, never())
                .enqueueInterviewScheduled(any());
    }

    // ============================================================
    // Schedule Interview - interviewer not found
    // ============================================================

    @Test
    void scheduleInterview_shouldThrowWhenInterviewerNotFound() {

        ScheduleInterviewRequest request =
                new ScheduleInterviewRequest(
                        1L,
                        Set.of(10L, 20L),
                        startTime,
                        60,
                        InterviewRound.SCREENING,
                        InterviewMode.VIRTUAL,
                        null
                );

        Interviewer interviewer2 = new Interviewer();
        interviewer2.setId(20L);
        interviewer2.setName("Second Interviewer");

        InterviewerAvailability availability1 =
                mockAvailability(interviewer, 9, 0, 18, 0);

        InterviewerAvailability availability2 =
                mockAvailability(interviewer2, 9, 0, 18, 0);

        when(availabilityRepository
                .findByInterviewerIdInAndDayOfWeek(
                        eq(Set.of(10L, 20L)),
                        eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of(availability1, availability2));

        when(interviewRepository.existsConflict(
                eq(Set.of(10L, 20L)),
                eq(startTime),
                eq(endTime)))
                .thenReturn(false);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        // Only one returned although two requested
        when(interviewerRepository.findAllById(Set.of(10L, 20L)))
                .thenReturn(List.of(interviewer));

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService.scheduleInterview(request)
        );

        verify(interviewRepository, never())
                .save(any());

        verify(eventDispatchService, never())
                .enqueueInterviewScheduled(any());
    }

    // ============================================================
    // Submit Feedback - SUCCESS
    // ============================================================

    @Test
    void submitFeedback_shouldSaveFeedbackAndCompleteInterview() {

        Long interviewId = 100L;

        Recommendation recommendation =
                mock(Recommendation.class);

        SubmitFeedbackRequest request =
                new SubmitFeedbackRequest(
                        10L,
                        5,
                        "Strong candidate",
                        recommendation
                );

        Interview interview = new Interview();

        interview.setId(interviewId);
        interview.setCandidate(candidate);
        interview.setInterviewers(
                new HashSet<>(Set.of(interviewer))
        );
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setStartTime(startTime);
        interview.setEndTime(endTime);
        interview.setRound(InterviewRound.SCREENING);
        interview.setMode(InterviewMode.VIRTUAL);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        when(feedbackRepository
                .existsByInterview_IdAndInterviewer_Id(
                        interviewId,
                        10L))
                .thenReturn(false);

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(interviewRepository.save(any(Interview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterviewResponse response =
                interviewService.submitFeedback(
                        interviewId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                InterviewStatus.COMPLETED,
                interview.getStatus()
        );

        assertNotNull(interview.getFeedback());

        assertSame(
                interview,
                interview.getFeedback().getInterview()
        );

        assertSame(
                interviewer,
                interview.getFeedback().getInterviewer()
        );

        assertEquals(
                5,
                interview.getFeedback().getRating()
        );

        assertEquals(
                "Strong candidate",
                interview.getFeedback().getComments()
        );

        assertEquals(
                recommendation,
                interview.getFeedback().getRecommendation()
        );

        assertNotNull(
                interview.getFeedback().getSubmittedAt()
        );

        verify(feedbackRepository)
                .save(any(Feedback.class));

        verify(interviewRepository)
                .save(interview);
    }

    // ============================================================
    // Submit Feedback - cancelled interview
    // ============================================================

    @Test
    void submitFeedback_shouldRejectCancelledInterview() {

        Long interviewId = 100L;

        SubmitFeedbackRequest request =
                mock(SubmitFeedbackRequest.class);

        when(request.interviewerId()).thenReturn(10L);

        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setCandidate(candidate);
        interview.setInterviewers(
                new HashSet<>(Set.of(interviewer))
        );
        interview.setStatus(InterviewStatus.CANCELLED);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        assertThrows(
                InvalidInterviewStateException.class,
                () -> interviewService.submitFeedback(
                        interviewId,
                        request
                )
        );

        verify(feedbackRepository, never()).save(any());
        verify(interviewRepository, never()).save(any());
    }

    // ============================================================
    // Submit Feedback - interviewer not part of panel
    // ============================================================

    @Test
    void submitFeedback_shouldRejectInterviewerNotInPanel() {

        Long interviewId = 100L;

        SubmitFeedbackRequest request =
                mock(SubmitFeedbackRequest.class);

        when(request.interviewerId()).thenReturn(999L);

        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setCandidate(candidate);
        interview.setInterviewers(
                new HashSet<>(Set.of(interviewer))
        );
        interview.setStatus(InterviewStatus.SCHEDULED);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        assertThrows(
                InvalidInterviewStateException.class,
                () -> interviewService.submitFeedback(
                        interviewId,
                        request
                )
        );

        verify(feedbackRepository, never())
                .existsByInterview_IdAndInterviewer_Id(
                        anyLong(),
                        anyLong()
                );

        verify(feedbackRepository, never()).save(any());
    }

    // ============================================================
    // Submit Feedback - duplicate
    // ============================================================

    @Test
    void submitFeedback_shouldRejectDuplicateFeedback() {

        Long interviewId = 100L;

        SubmitFeedbackRequest request =
                mock(SubmitFeedbackRequest.class);

        when(request.interviewerId()).thenReturn(10L);

        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setCandidate(candidate);
        interview.setInterviewers(
                new HashSet<>(Set.of(interviewer))
        );
        interview.setStatus(InterviewStatus.SCHEDULED);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        when(feedbackRepository
                .existsByInterview_IdAndInterviewer_Id(
                        interviewId,
                        10L))
                .thenReturn(true);

        assertThrows(
                DuplicateFeedbackException.class,
                () -> interviewService.submitFeedback(
                        interviewId,
                        request
                )
        );

        verify(feedbackRepository, never()).save(any());
        verify(interviewRepository, never()).save(any());
    }

    // ============================================================
    // Submit Feedback - interview not found
    // ============================================================

    @Test
    void submitFeedback_shouldThrowWhenInterviewNotFound() {

        Long interviewId = 999L;

        SubmitFeedbackRequest request =
                mock(SubmitFeedbackRequest.class);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService.submitFeedback(
                        interviewId,
                        request
                )
        );

        verify(feedbackRepository, never()).save(any());
    }

    // ============================================================
    // Search Interviews
    // ============================================================

    @Test
    void searchInterviews_shouldReturnPaginatedResults() {

        Pageable pageable = PageRequest.of(0, 10);

        Interview interview = new Interview();
        interview.setId(100L);
        interview.setCandidate(candidate);
        interview.setInterviewers(
                new HashSet<>(Set.of(interviewer))
        );
        interview.setStartTime(startTime);
        interview.setEndTime(endTime);
        interview.setRound(InterviewRound.SCREENING);
        interview.setMode(InterviewMode.VIRTUAL);
        interview.setStatus(InterviewStatus.SCHEDULED);

        Page<Interview> page =
                new PageImpl<>(
                        List.of(interview),
                        pageable,
                        1
                );

        when(interviewRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                eq(pageable)))
                .thenReturn(page);

        Page<InterviewResponse> result =
                interviewService.searchInterviews(
                        "John",
                        "Jane",
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        InterviewResponse response =
                result.getContent().get(0);

        assertEquals(100L, response.id());
        assertEquals(1L, response.candidateId());
        assertEquals("John Candidate", response.candidateName());
        assertEquals(
                List.of("Jane Interviewer"),
                response.interviewerNames()
        );

        verify(interviewRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                eq(pageable)
        );
    }

    // ============================================================
    // Helpers
    // ============================================================

    private InterviewerAvailability mockAvailability(
            Interviewer interviewer,
            int startHour,
            int startMinute,
            int endHour,
            int endMinute) {

        InterviewerAvailability availability =
                mock(InterviewerAvailability.class);

        when(availability.getInterviewer())
                .thenReturn(interviewer);

        when(availability.getStartTime())
                .thenReturn(LocalTime.of(startHour, startMinute));

        when(availability.getEndTime())
                .thenReturn(LocalTime.of(endHour, endMinute));

        return availability;
    }
}

