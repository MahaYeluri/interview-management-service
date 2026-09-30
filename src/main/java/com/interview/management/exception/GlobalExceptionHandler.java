package com.interview.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
    ErrorResponse errorResponse=new ErrorResponse();
    errorResponse.setError("NOT FOUND");
    errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
    errorResponse.setMessage(ex.getMessage());
    errorResponse.setTimestamp(LocalDateTime.now());
    return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(InterviewerUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleInterviewerUnavailableException(InterviewerUnavailableException ex) {
        ErrorResponse errorResponse=new ErrorResponse();
        errorResponse.setError("Interviewer NOT Available");
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse errorResponse=new ErrorResponse();
        errorResponse.setError("Internal Server Error");
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidInterviewStateException.class)
    public ResponseEntity<ErrorResponse> unprocessable(InvalidInterviewStateException ex) {
        ErrorResponse errorResponse=new ErrorResponse();
        errorResponse.setError("InvalidInterviewStateException");
        errorResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse,HttpStatus.UNPROCESSABLE_ENTITY);

    }
}
