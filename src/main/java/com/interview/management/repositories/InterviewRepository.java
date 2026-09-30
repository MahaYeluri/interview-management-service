package com.interview.management.repositories;

import com.interview.management.models.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface InterviewRepository extends JpaRepository<Interview, Long>, JpaSpecificationExecutor<Interview> {

    @Query("""
        SELECT COUNT(i) > 0 
        FROM Interview i JOIN i.interviewers iv
        WHERE iv.id IN :interviewerIds
          AND i.status = com.interview.management.enums.InterviewStatus.SCHEDULED
          AND i.startTime < :end 
          AND i.endTime > :start
        """)
    boolean existsConflict(
            @Param("interviewerIds") Set<Long> interviewerIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}