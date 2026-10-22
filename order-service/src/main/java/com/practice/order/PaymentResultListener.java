package com.practice.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Queue consumer: reads the payment result and updates the order
@Component
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final PubSubTemplate pubSub;
    private final ObjectMapper mapper;
    private final OrderRepository orders;
    private final InventoryClient inventory;

    public PaymentResultListener(PubSubTemplate pubSub, ObjectMapper mapper,
                                 OrderRepository orders, InventoryClient inventory) {
        this.pubSub = pubSub;
        this.mapper = mapper;
        this.orders = orders;
        this.inventory = inventory;
    }

    @PostConstruct
    void start() {
        pubSub.subscribe("payment-completed-order-sub", this::handle);
        log.info("Listening on subscription payment-completed-order-sub");
    }

    private void handle(BasicAcknowledgeablePubsubMessage message) {
        try {
            String json = message.getPubsubMessage().getData().toStringUtf8();
            PaymentCompletedEvent evt = mapper.readValue(json, PaymentCompletedEvent.class);
            log.info("Received payment result: {}", evt);

            orders.findById(evt.orderId()).ifPresent(o -> {
                if (!"PENDING_PAYMENT".equals(o.status)) return;   // duplicate delivery: ignore
                if ("SUCCESS".equals(evt.status())) {
                    o.status = "PAID";
                } else {
                    o.status = "PAYMENT_FAILED";
                    inventory.release(o.productId, o.quantity);    // REST compensation: put stock back
                }
                orders.save(o);
            });
            message.ack();
        } catch (Exception e) {
            log.error("Failed to process payment result, will be redelivered", e);
            message.nack();
        }
    }
}
