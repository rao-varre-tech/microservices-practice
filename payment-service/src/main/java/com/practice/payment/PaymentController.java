package com.practice.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PaymentController {

    private final PaymentRepository payments;

    public PaymentController(PaymentRepository payments) {
        this.payments = payments;
    }

    @GetMapping("/payments")
    public List<Payment> list() {
        return payments.findAll();
    }

    @GetMapping("/payments/{id}")
    public Payment get(@PathVariable Long id) {
        return payments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @GetMapping("/payments/order/{orderId}")
    public Payment getByOrder(@PathVariable Long orderId) {
        return payments.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No payment for this order"));
    }
}
