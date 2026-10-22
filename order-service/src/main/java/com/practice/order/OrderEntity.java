package com.practice.order;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public Long productId;
    public int quantity;
    public BigDecimal amount;
    public String status;   // PENDING_PAYMENT, PAID, PAYMENT_FAILED
}
