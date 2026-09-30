--liquibase formatted sql

--changeset interview-team:008-interviewer-availability
CREATE TABLE interviewer_availability (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    interviewer_id  BIGINT      NOT NULL,
    day_of_week     VARCHAR(10) NOT NULL,
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    CONSTRAINT fk_avail_interviewer FOREIGN KEY (interviewer_id) REFERENCES interviewer (id),
    CONSTRAINT ck_avail_time CHECK (end_time > start_time),
    CONSTRAINT ck_avail_day  CHECK (day_of_week IN
        ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'))
);
CREATE INDEX idx_avail_lookup ON interviewer_availability (interviewer_id, day_of_week);
--rollback DROP TABLE interviewer_availability;