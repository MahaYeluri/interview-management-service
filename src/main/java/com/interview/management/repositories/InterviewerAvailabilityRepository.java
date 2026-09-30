package com.interview.management.repositories;

import com.interview.management.models.InterviewerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface InterviewerAvailabilityRepository extends JpaRepository<InterviewerAvailability,Long> {

    List<InterviewerAvailability> findByInterviewerId(Long interviewerId);

    List<InterviewerAvailability> findByInterviewerIdInAndDayOfWeek(Set<Long> ids, DayOfWeek day);

    @Query("""
        select count(a) > 0 from InterviewerAvailability a
        where a.interviewer.id = :interviewerId and a.dayOfWeek = :day
          and a.startTime < :end and a.endTime > :start
        """)
    boolean existsOverlap(Long interviewerId, DayOfWeek day, LocalTime start, LocalTime end);

}
