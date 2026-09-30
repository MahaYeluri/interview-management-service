package com.interview.management.exception;

public class SchedulingConflictException extends RuntimeException{
    public SchedulingConflictException(String message){
        super(message);
    }
}
