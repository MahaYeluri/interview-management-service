package com.interview.management.models;

import com.interview.management.enums.InterviewMode;
import com.interview.management.enums.InterviewRound;
import com.interview.management.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Candidate candidate;

    @ManyToMany
    @JoinTable(name = "interview_interviewers",
            joinColumns = @JoinColumn(name = "interview_id"),
            inverseJoinColumns = @JoinColumn(name = "interviewer_id"))
    private Set<Interviewer> interviewers = new HashSet<>();

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;

    @Enumerated(EnumType.STRING) private InterviewRound round;
    @Enumerated(EnumType.STRING) private InterviewMode mode;
    @Enumerated(EnumType.STRING) private InterviewStatus status;

//    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL)
    private Feedback feedback;

//    public void addFeedback(Feedback f) { feedbacks.add(f); f.setInterview(this); }

}
