package com.interview.management.controllers;

import com.interview.management.dto.InterviewResponse;
import com.interview.management.dto.ScheduleInterviewRequest;
import com.interview.management.dto.SubmitFeedbackRequest;
import com.interview.management.services.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/interviews")
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> scheduleInterview(@RequestBody ScheduleInterviewRequest request) {
       log.info("------------Inside scheduleInterview for cndidate id : {} and interviewer id : {} ----------",request.candidateId(),request.interviewerIds());
        InterviewResponse response = interviewService.scheduleInterview(request);
        log.info("------------Inside scheduleInterview completed--------------");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{interviewId}/feedback")
    public ResponseEntity<InterviewResponse> submitFeedback(
            @PathVariable Long interviewId,
            @RequestBody SubmitFeedbackRequest request) {
        InterviewResponse response = interviewService.submitFeedback(interviewId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<InterviewResponse>> searchInterviews(
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String interviewerName,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("------------Inside searchInterviews for candidateName={}, interviewerName={},pageable={}",candidateName,interviewerName,pageable);
        Page<InterviewResponse> interviews = interviewService.searchInterviews(candidateName, interviewerName, pageable);
        log.info("------------Inside searchInterviews completed--------------------");
        return ResponseEntity.ok(interviews);
    }
}
