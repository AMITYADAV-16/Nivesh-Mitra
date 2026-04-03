package com.example.whatsapp_service;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "user-profile-service")
public interface UserProfileClient {

    @PostMapping("/users")
    UserProfile createUser(@RequestBody UserProfile user);

    @GetMapping("/users/{id}")
    UserProfile getUserById(@PathVariable Long id);

    // ADD THESE:
    @GetMapping("/users/phone/{phone}")
    UserProfile getUserByPhone(@PathVariable String phone);

    @GetMapping("/users")
    List<UserProfile> getAllUsers();

    @Data
    class UserProfile {
        private Long id;
        private String name;
        private Integer age;
        private Double monthlyIncome;
        private Double monthlyExpenses;
        private Integer dependents;
        private String goal;
        private String preferredLanguage;
        private Double targetAmount;
        private Integer targetYears;
        private String phone;   // ADD THIS
    }
}