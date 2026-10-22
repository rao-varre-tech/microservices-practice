package com.practice.payment;

// Published to topic "payment-completed"
public record PaymentCompletedEvent(Long orderId, Long paymentId, String status) {
}
