package com.interview.management.repositories;

import com.interview.management.enums.DispatchStatus;
import com.interview.management.models.EventDispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventDispatchRepository extends JpaRepository<EventDispatch, Long> {
    List<EventDispatch> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            DispatchStatus status, LocalDateTime now);
}