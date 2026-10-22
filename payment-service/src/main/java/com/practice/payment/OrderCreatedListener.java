package com.practice.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Queue consumer: reads new orders, does the (fake) payment, publishes the result
@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);
    private static final BigDecimal FAIL_ABOVE = new BigDecimal("50000");   // demo rule

    private final PubSubTemplate pubSub;
    private final ObjectMapper mapper;
    private final PaymentRepository payments;
    private final EventPublisher publisher;

    public OrderCreatedListener(PubSubTemplate pubSub, ObjectMapper mapper,
                                PaymentRepository payments, EventPublisher publisher) {
        this.pubSub = pubSub;
        this.mapper = mapper;
        this.payments = payments;
        this.publisher = publisher;
    }

    @PostConstruct
    void start() {
        pubSub.subscribe("order-created-payment-sub", this::handle);
        log.info("Listening on subscription order-created-payment-sub");
    }

    private void handle(BasicAcknowledgeablePubsubMessage message) {
        try {
            String json = message.getPubsubMessage().getData().toStringUtf8();
            OrderCreatedEvent evt = mapper.readValue(json, OrderCreatedEvent.class);
            log.info("Received order: {}", evt);

            if (!payments.existsByOrderId(evt.orderId())) {     // idempotent: Pub/Sub can deliver twice
                Payment p = new Payment();
                p.orderId = evt.orderId();
                p.amount = evt.amount();
                p.status = evt.amount().compareTo(FAIL_ABOVE) > 0 ? "FAILED" : "SUCCESS";
                p = payments.save(p);

                publisher.publish("payment-completed",
                        new PaymentCompletedEvent(p.orderId, p.id, p.status));
            }
            message.ack();
        } catch (Exception e) {
            log.error("Failed to process order, will be redelivered", e);
            message.nack();
        }
    }
}
