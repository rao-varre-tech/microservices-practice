package com.practice.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class OrderController {

    public record CreateUserRequest(String name) {}
    public record PlaceOrderRequest(Long userId, Long productId, int quantity) {}

    private final UserRepository users;
    private final OrderRepository orders;
    private final InventoryClient inventory;
    private final EventPublisher publisher;

    public OrderController(UserRepository users, OrderRepository orders,
                           InventoryClient inventory, EventPublisher publisher) {
        this.users = users;
        this.orders = orders;
        this.inventory = inventory;
        this.publisher = publisher;
    }

    @PostMapping("/users")
    public AppUser createUser(@RequestBody CreateUserRequest req) {
        AppUser u = new AppUser();
        u.name = req.name();
        return users.save(u);
    }

    @GetMapping("/users")
    public List<AppUser> listUsers() {
        return users.findAll();
    }

    @PostMapping("/orders")
    public OrderEntity placeOrder(@RequestBody PlaceOrderRequest req) {
        users.findById(req.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 1) REST call to inventory-service: check and reserve stock
        InventoryClient.ReserveResponse reserved;
        try {
            reserved = inventory.reserve(req.productId(), req.quantity());
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "Inventory says: " + e.getStatusText());
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service is not reachable");
        }

        // 2) Save the order
        OrderEntity o = new OrderEntity();
        o.userId = req.userId();
        o.productId = req.productId();
        o.quantity = req.quantity();
        o.amount = reserved.price().multiply(BigDecimal.valueOf(req.quantity()));
        o.status = "PENDING_PAYMENT";
        o = orders.save(o);

        // 3) Queue: tell payment-service to charge for this order
        publisher.publish("order-created",
                new OrderCreatedEvent(o.id, o.userId, o.productId, o.quantity, o.amount));

        return o;
    }

    @GetMapping("/orders")
    public List<OrderEntity> listOrders() {
        return orders.findAll();
    }

    @GetMapping("/orders/{id}")
    public OrderEntity getOrder(@PathVariable Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
