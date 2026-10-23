package com.practice.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final PubSubTemplate pubSub;
    private final ObjectMapper mapper;

    public EventPublisher(PubSubTemplate pubSub, ObjectMapper mapper) {
        this.pubSub = pubSub;
        this.mapper = mapper;
    }

    public void publish(String topic, Object event) {
        try {
            pubSub.publish(topic, mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event..", e);
        }
    }
}
