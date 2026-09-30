--liquibase formatted sql

--changeset interview-team:011-event-dispatch-queue
CREATE TABLE event_dispatch_queue (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id         VARCHAR(36)  NOT NULL,
    aggregate_type   VARCHAR(50)  NOT NULL,
    aggregate_id     BIGINT       NOT NULL,
    event_type       VARCHAR(50)  NOT NULL,
    payload          CLOB         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    attempts         INT          NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMP    NOT NULL,
    last_error       VARCHAR(1000),
    created_at       TIMESTAMP    NOT NULL,
    dispatched_at    TIMESTAMP,
    CONSTRAINT uq_dispatch_event_id UNIQUE (event_id)
);
CREATE INDEX idx_dispatch_poll ON event_dispatch_queue (status, next_attempt_at);
--rollback DROP TABLE event_dispatch_queue;