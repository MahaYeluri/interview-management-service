package com.interview.management.services;

import com.interview.management.dto.InterviewResponse;
import com.interview.management.dto.ScheduleInterviewRequest;
import com.interview.management.dto.SubmitFeedbackRequest;
import com.interview.management.enums.InterviewMode;
import com.interview.management.enums.InterviewRound;
import com.interview.management.enums.InterviewStatus;
import com.interview.management.exception.DuplicateFeedbackException;
import com.interview.management.exception.InterviewerUnavailableException;
import com.interview.management.exception.InvalidInterviewStateException;
import com.interview.management.exception.ResourceNotFoundException;
import com.interview.management.exception.SchedulingConflictException;
import com.interview.management.models.Candidate;
import com.interview.management.models.Feedback;
import com.interview.management.models.Interview;
import com.interview.management.models.Interviewer;
import com.interview.management.models.InterviewerAvailability;
import com.interview.management.notification.EventDispatchService;
import com.interview.management.repositories.CandidateRepository;
import com.interview.management.repositories.FeedbackRepository;
import com.interview.management.repositories.InterviewRepository;
import com.interview.management.repositories.InterviewSpecification;
import com.interview.management.repositories.InterviewerAvailabilityRepository;
import com.interview.management.repositories.InterviewerRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewerRepository interviewerRepository;
    private final InterviewerAvailabilityRepository availabilityRepository;
    private final FeedbackRepository feedbackRepository;
    private final EventDispatchService eventDispatchService;


    // ============================================================
    // Schedule Interview
    // ============================================================

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(ScheduleInterviewRequest request) {

        log.info("Scheduling interview for candidateId={}, interviewerIds={}",
                request.candidateId(),
                request.interviewerIds());

        Set<Long> interviewerIds = new HashSet<>(request.interviewerIds());

        // --------------------------------------------------------
        // 1. Validate requested time
        // --------------------------------------------------------

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(request.durationMinutes());

        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        LocalTime requestedStart = startTime.toLocalTime();
        LocalTime requestedEnd = endTime.toLocalTime();

        log.info(
                "Interview requested: start={}, end={}, day={}",
                startTime,
                endTime,
                dayOfWeek
        );

        // --------------------------------------------------------
        // 2. Check interviewer working availability
        // --------------------------------------------------------

        List<InterviewerAvailability> availableSlots =
                availabilityRepository.findByInterviewerIdInAndDayOfWeek(
                        interviewerIds,
                        dayOfWeek
                );

        Set<Long> availableInterviewerIds = availableSlots.stream()
                .filter(slot ->
                        !slot.getStartTime().isAfter(requestedStart)
                                && !slot.getEndTime().isBefore(requestedEnd)
                )
                .map(slot -> slot.getInterviewer().getId())
                .collect(Collectors.toSet());

        if (!availableInterviewerIds.containsAll(interviewerIds)) {
            throw new InterviewerUnavailableException(
                    "One or more interviewers are not available during the requested time."
            );
        }

        // --------------------------------------------------------
        // 3. Check interviewer booking conflict
        // --------------------------------------------------------

        boolean hasConflict = interviewRepository.existsConflict(
                interviewerIds,
                startTime,
                endTime
        );

        if (hasConflict) {
            throw new SchedulingConflictException(
                    "An interviewer is already booked during this time."
            );
        }

        // --------------------------------------------------------
        // 4. Find candidate
        // --------------------------------------------------------

        Candidate candidate = candidateRepository
                .findById(request.candidateId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found with ID: "
                                        + request.candidateId()
                        )
                );

        // --------------------------------------------------------
        // 5. Find interviewers
        // --------------------------------------------------------

        List<Interviewer> interviewers =
                interviewerRepository.findAllById(interviewerIds);

        if (interviewers.size() != interviewerIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more interviewers were not found."
            );
        }

        // --------------------------------------------------------
        // 6. Create Interview
        // --------------------------------------------------------

        Interview interview = new Interview();

        interview.setCandidate(candidate);
        interview.setInterviewers(new HashSet<>(interviewers));
        interview.setStartTime(startTime);
        interview.setEndTime(endTime);
        interview.setMeetingLink(request.meetingLink());

        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setMode(InterviewMode.VIRTUAL);
        interview.setRound(InterviewRound.SCREENING);

        log.info(
                "Saving interview: candidateId={}, start={}, end={}",
                candidate.getId(),
                startTime,
                endTime
        );

        Interview savedInterview = interviewRepository.save(interview);

        eventDispatchService.enqueueInterviewScheduled(savedInterview);   // outbox row, same transaction

        return mapToResponse(savedInterview);

    }


    // ============================================================
    // Submit Feedback
    // ============================================================

    @Override
    @Transactional
    public InterviewResponse submitFeedback(
            Long interviewId,
            SubmitFeedbackRequest request) {

        log.info(
                "Submitting feedback for interviewId={}, interviewerId={}",
                interviewId,
                request.interviewerId()
        );

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Interview not found with id: " + interviewId
                        )
                );

        // --------------------------------------------------------
        // 1. Validate interview state
        // --------------------------------------------------------

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new InvalidInterviewStateException(
                    "Cannot add feedback to a cancelled interview."
            );
        }

        // --------------------------------------------------------
        // 2. Validate interviewer belongs to interview
        // --------------------------------------------------------

        Interviewer interviewer = interview.getInterviewers()
                .stream()
                .filter(i ->
                        i.getId().equals(request.interviewerId())
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidInterviewStateException(
                                "Interviewer is not part of this interview panel."
                        )
                );

        // --------------------------------------------------------
        // 3. Prevent duplicate feedback
        // --------------------------------------------------------

        if (feedbackRepository.existsByInterview_IdAndInterviewer_Id(
                interviewId,
                request.interviewerId())) {

            throw new DuplicateFeedbackException(
                    "Feedback already submitted by this interviewer."
            );
        }

        // --------------------------------------------------------
        // 4. Create feedback
        // --------------------------------------------------------

        Feedback feedback = new Feedback();



        feedback.setInterview(interview);
        feedback.setInterviewer(interviewer);
        feedback.setRating(request.rating());
        feedback.setComments(request.comments());
        feedback.setRecommendation(request.recommendation());
        feedback.setSubmittedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);



        /*
         * If your Feedback entity has these relationships,
         * set them here:
         *
         * feedback.setInterview(interview);
         * feedback.setInterviewer(interviewer);
         */

        // --------------------------------------------------------
        // 5. Update interview
        // --------------------------------------------------------

        interview.setFeedback(feedback);
        interview.setStatus(InterviewStatus.COMPLETED);

        Interview updatedInterview =
                interviewRepository.save(interview);

        return mapToResponse(updatedInterview);
    }


    // ============================================================
    // Search Interviews
    // ============================================================


    @Override
    @Transactional(readOnly = true)
    public Page<InterviewResponse> searchInterviews(
            String candidateName,
            String interviewerName,
            Pageable pageable) {

        log.info(
                "Searching interviews: candidateName={}, interviewerName={}, page={}, size={}",
                candidateName,
                interviewerName,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        // Build specifications
        Specification<Interview> spec =InterviewSpecification.hasCandidateName(candidateName)
                .and(InterviewSpecification.hasInterviewerName(interviewerName));

        Page<Interview> interviews = interviewRepository.findAll(spec, pageable);

        log.info("Total elements found: {}", interviews.getTotalElements());

        return interviews.map(this::mapToResponse);
    }

    // ============================================================
    // Entity -> Response DTO
    // ============================================================

    private InterviewResponse mapToResponse(Interview interview) {

        List<String> interviewerNames =
                interview.getInterviewers()
                        .stream()
                        .map(Interviewer::getName)
                        .toList();

        return new InterviewResponse(
                interview.getId(),
                interview.getCandidate().getId(),
                interview.getCandidate().getName(),
                interviewerNames,
                interview.getStartTime(),
                interview.getEndTime(),
                interview.getRound(),
                interview.getMode(),
                interview.getStatus()
        );
    }
}

