package com.practice.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class InventoryController {

    public record CreateProductRequest(String name, BigDecimal price, int stock) {}
    public record ProductView(Long id, String name, BigDecimal price, int stock) {}
    public record StockRequest(Long productId, int quantity) {}
    public record ReserveResponse(Long productId, BigDecimal price, int quantity) {}

    private final ProductRepository products;
    private final InventoryRepository inventory;

    public InventoryController(ProductRepository products, InventoryRepository inventory) {
        this.products = products;
        this.inventory = inventory;
    }

    @PostMapping("/products")
    @Transactional
    public ProductView createProduct(@RequestBody CreateProductRequest req) {
        Product p = new Product();
        p.name = req.name();
        p.price = req.price();
        p = products.save(p);

        Inventory i = new Inventory();
        i.productId = p.id;
        i.quantity = req.stock();
        inventory.save(i);

        return new ProductView(p.id, p.name, p.price, i.quantity);
    }

    @GetMapping("/products")
    public List<ProductView> listProducts() {
        return products.findAll().stream().map(this::view).toList();
    }

    @GetMapping("/products/{id}")
    public ProductView getProduct(@PathVariable Long id) {
        return view(findProduct(id));
    }

    // Manage inventory: set stock to an absolute number
    @PutMapping("/inventory/{productId}")
    @Transactional
    public ProductView setStock(@PathVariable Long productId, @RequestParam int quantity) {
        Product p = findProduct(productId);
        Inventory i = inventory.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No inventory row"));
        i.quantity = quantity;
        inventory.save(i);
        return view(p);
    }

    // Called by order-service over REST
    @PostMapping("/inventory/reserve")
    @Transactional
    public ReserveResponse reserve(@RequestBody StockRequest req) {
        Product p = findProduct(req.productId());
        int updated = inventory.reserve(req.productId(), req.quantity());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        return new ReserveResponse(p.id, p.price, req.quantity());
    }

    // Compensation: called by order-service when payment fails
    @PostMapping("/inventory/release")
    @Transactional
    public void release(@RequestBody StockRequest req) {
        inventory.release(req.productId(), req.quantity());
    }

    private Product findProduct(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductView view(Product p) {
        int stock = inventory.findByProductId(p.id).map(i -> i.quantity).orElse(0);
        return new ProductView(p.id, p.name, p.price, stock);
    }
}
