package com.practice.inventory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository products;
    private final InventoryRepository inventory;

    public DataSeeder(ProductRepository products, InventoryRepository inventory) {
        this.products = products;
        this.inventory = inventory;
    }

    @Override
    public void run(String... args) {
        if (products.count() > 0) return;
        add("Laptop", new BigDecimal("60000"), 10);   // id 1 (expensive: payment will FAIL in the demo)
        add("LG Mouse", new BigDecimal("500"), 100);     // id 2 (payment will succeed)
    }

    private void add(String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.name = name;
        p.price = price;
        p = products.save(p);
        Inventory i = new Inventory();
        i.productId = p.id;
        i.quantity = stock;
        inventory.save(i);
    }
}
