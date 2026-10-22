package com.practice.order;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;

    public DataSeeder(UserRepository users) {
        this.users = users;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        AppUser u = new AppUser();
        u.name = "Test User1";   // id 1
        users.save(u);
    }
}
