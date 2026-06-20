package com.fabriciosanches.adapter.outbound.http;

import com.fabriciosanches.domain.port.output.NotificationClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClientAdapter implements NotificationClientPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientAdapter.class);

    private final RestClient restClient;

    public NotificationClientAdapter(RestClient.Builder builder,
                                     @Value("${clients.notification.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void notifyOrderApproved(String orderId, String customerId) {
        try {
            restClient.post()
                    .uri("/notifications")
                    .body(new NotificationRequest(orderId, customerId, "ORDER_APPROVED"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send notification for orderId={}. Continuing without notification.", orderId, ex);
        }
    }

    record NotificationRequest(String orderId, String customerId, String eventType) {
    }
}
