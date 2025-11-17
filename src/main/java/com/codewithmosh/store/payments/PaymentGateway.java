package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.Order;

import java.util.Optional;

public interface PaymentGateway {
    CheckotSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookEvent(WebhookRequest request);
}
