package com.practice.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long orderId;
    public BigDecimal amount;
    public String status;   // SUCCESS or FAILED
}
