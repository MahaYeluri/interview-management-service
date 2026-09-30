package com.interview.management.repositories;

import com.interview.management.models.Interviewer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewerRepository extends JpaRepository<Interviewer,Long> {
}
