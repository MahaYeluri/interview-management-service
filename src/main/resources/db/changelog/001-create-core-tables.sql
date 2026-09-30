--liquibase formatted sql

--changeset interview-team:001-candidate
CREATE TABLE candidate (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    email   VARCHAR(150) NOT NULL,
    phone   VARCHAR(20),
    skill   VARCHAR(2000),
    CONSTRAINT uq_candidate_email UNIQUE (email)
);
--rollback DROP TABLE candidate;

--changeset interview-team:002-interviewer
CREATE TABLE interviewer (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    department  VARCHAR(100),
    CONSTRAINT uq_interviewer_email UNIQUE (email)
);
--rollback DROP TABLE interviewer;

--changeset interview-team:003-interview
CREATE TABLE interview (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id  BIGINT       NOT NULL,
    start_time    TIMESTAMP    NOT NULL,
    end_time      TIMESTAMP    NOT NULL,
    meeting_link  VARCHAR(300),
    round         VARCHAR(20)  NOT NULL,
    mode          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES candidate (id),
    CONSTRAINT ck_interview_time   CHECK (end_time > start_time),
    CONSTRAINT ck_interview_round  CHECK (round  IN ('SCREENING','TECHNICAL','MANAGERIAL','HR')),
    CONSTRAINT ck_interview_mode   CHECK (mode   IN ('VIRTUAL','INPERSON','TELEPHONIC')),
    CONSTRAINT ck_interview_status CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','RESCHEDULED'))
);
CREATE INDEX idx_interview_candidate ON interview (candidate_id);
CREATE INDEX idx_interview_time      ON interview (start_time, end_time);
CREATE INDEX idx_interview_status    ON interview (status);
--rollback DROP TABLE interview;

--changeset interview-team:004-interview-interviewers
CREATE TABLE interview_interviewers (
    interview_id    BIGINT NOT NULL,
    interviewer_id  BIGINT NOT NULL,
    PRIMARY KEY (interview_id, interviewer_id),
    CONSTRAINT fk_ii_interview   FOREIGN KEY (interview_id)   REFERENCES interview (id),
    CONSTRAINT fk_ii_interviewer FOREIGN KEY (interviewer_id) REFERENCES interviewer (id)
);
CREATE INDEX idx_ii_interviewer ON interview_interviewers (interviewer_id);
--rollback DROP TABLE interview_interviewers;

--changeset interview-team:005-feedback
CREATE TABLE feedback (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id    BIGINT        NOT NULL,
    interviewer_id  BIGINT        NOT NULL,
    rating          INT           NOT NULL,
    comments        VARCHAR(2000),
    recommendation  VARCHAR(20)   NOT NULL,
    submitted_at    TIMESTAMP     NOT NULL,
    CONSTRAINT fk_feedback_interview   FOREIGN KEY (interview_id)   REFERENCES interview (id),
    CONSTRAINT fk_feedback_interviewer FOREIGN KEY (interviewer_id) REFERENCES interviewer (id),
    CONSTRAINT uq_feedback_per_panelist UNIQUE (interview_id, interviewer_id),
    CONSTRAINT ck_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_feedback_reco   CHECK (recommendation IN ('STRONG_HIRE','HIRE','HOLD','NO_HIRE'))
);
--rollback DROP TABLE feedback;