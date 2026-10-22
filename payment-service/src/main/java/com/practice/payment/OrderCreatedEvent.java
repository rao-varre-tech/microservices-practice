package com.practice.payment;

import java.math.BigDecimal;

// Consumed from topic "order-created"
public record OrderCreatedEvent(Long orderId, Long userId, Long productId, int quantity, BigDecimal amount) {
}
