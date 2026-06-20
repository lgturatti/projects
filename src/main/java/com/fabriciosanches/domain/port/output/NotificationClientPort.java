package com.fabriciosanches.domain.port.output;

public interface NotificationClientPort {

    void notifyOrderApproved(String orderId, String customerId);
}
