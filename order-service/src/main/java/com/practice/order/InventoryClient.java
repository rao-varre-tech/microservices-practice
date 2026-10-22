package com.practice.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

// Synchronous REST call from order-service to inventory-service
@Component
public class InventoryClient {

    public record StockRequest(Long productId, int quantity) {}
    public record ReserveResponse(Long productId, BigDecimal price, int quantity) {}

    private final RestTemplate rest;
    private final String baseUrl;

    public InventoryClient(RestTemplateBuilder builder, @Value("${inventory.url}") String baseUrl) {
        this.rest = builder.build();
        this.baseUrl = baseUrl;
    }

    public ReserveResponse reserve(Long productId, int quantity) {
        return rest.postForObject(baseUrl + "/inventory/reserve",
                new StockRequest(productId, quantity), ReserveResponse.class);
    }

    public void release(Long productId, int quantity) {
        rest.postForObject(baseUrl + "/inventory/release",
                new StockRequest(productId, quantity), Void.class);
    }
}
