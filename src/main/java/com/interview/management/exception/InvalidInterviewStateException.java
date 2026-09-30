package com.interview.management.exception;

public class InvalidInterviewStateException extends RuntimeException {
    public InvalidInterviewStateException(String message) {
        super(message);
    }
}
