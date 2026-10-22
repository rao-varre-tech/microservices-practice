package com.practice.order;

// Consumed from topic "payment-completed". status = SUCCESS or FAILED
public record PaymentCompletedEvent(Long orderId, Long paymentId, String status) {
}
