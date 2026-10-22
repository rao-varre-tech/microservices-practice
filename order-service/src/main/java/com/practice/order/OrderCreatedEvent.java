package com.practice.order;

import java.math.BigDecimal;

// Published to topic "order-created"
public record OrderCreatedEvent(Long orderId, Long userId, Long productId, int quantity, BigDecimal amount) {
}
