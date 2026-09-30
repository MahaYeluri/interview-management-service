package com.interview.management.models;

import com.interview.management.enums.Recommendation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"interview_id", "interviewer_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "interview_id")
    private Interview interview;
//    @ManyToOne(fetch = FetchType.LAZY) private Interview interview;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private Interviewer interviewer;

    private int rating;
    private String comments;
    @Enumerated(EnumType.STRING) private Recommendation recommendation;
    private LocalDateTime submittedAt;
}