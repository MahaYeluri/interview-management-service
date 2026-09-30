package com.interview.management.models;
import com.interview.management.enums.DispatchStatus;
import com.interview.management.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
@Entity
@Table(name = "event_dispatch_queue")
@Getter
@NoArgsConstructor
public class EventDispatch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String aggregateType;
    private Long aggregateId;

    @Enumerated(EnumType.STRING) private EventType eventType;
    @Lob private String payload;
    @Enumerated(EnumType.STRING) private DispatchStatus status;

    private int attempts;
    private LocalDateTime nextAttemptAt;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;

    public static EventDispatch pending(String eventId, String aggregateType, Long aggregateId,
                                        EventType type, String payload) {
        EventDispatch e = new EventDispatch();
        e.eventId = eventId;
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.eventType = type;
        e.payload = payload;
        e.status = DispatchStatus.PENDING;
        e.createdAt = LocalDateTime.now();
        e.nextAttemptAt = e.createdAt;
        return e;
    }

    public void markDispatched() {
        status = DispatchStatus.DISPATCHED;
        dispatchedAt = LocalDateTime.now();
        lastError = null;
    }

    public void markAttemptFailed(String error, int maxAttempts) {
        attempts++;
        lastError = truncate(error);
        if (attempts >= maxAttempts) status = DispatchStatus.FAILED;
        else nextAttemptAt = LocalDateTime.now().plusSeconds(5L * (1L << attempts));   // 10s, 20s, 40s...
    }

    public void markPermanentlyFailed(String error) {
        attempts++;
        lastError = truncate(error);
        status = DispatchStatus.FAILED;
    }

    private static String truncate(String s) {
        return s == null ? null : s.substring(0, Math.min(s.length(), 1000));
    }
}