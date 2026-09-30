package com.interview.management.notification;


import com.interview.management.dto.NotificationRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {

    private final RestClient restClient;

    public NotificationClient(RestClient.Builder builder,
                              @Value("${notification.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public void send(NotificationRequest request) {
        restClient.post()
                .uri("/api/v1/notifications")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}