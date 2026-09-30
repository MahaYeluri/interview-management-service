package com.interview.management.repositories;

import com.interview.management.models.Interview;
import com.interview.management.models.Interviewer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class InterviewSpecification {

    public static Specification<Interview> hasCandidateName(String candidateName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(candidateName)) {
                return cb.conjunction(); // Returns true predicate (no filter)
            }
            return cb.like(
                    cb.lower(root.get("candidate").get("name")),
                    "%" + candidateName.toLowerCase().trim() + "%"
            );
        };
    }

    public static Specification<Interview> hasInterviewerName(String interviewerName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(interviewerName)) {
                return cb.conjunction(); // Returns true predicate (no filter)
            }
            query.distinct(true); // Prevents duplicate entity rows from ManyToMany join
            Join<Interview, Interviewer> interviewers = root.join("interviewers", JoinType.INNER);
            return cb.like(
                    cb.lower(interviewers.get("name")),
                    "%" + interviewerName.toLowerCase().trim() + "%"
            );
        };
    }
}