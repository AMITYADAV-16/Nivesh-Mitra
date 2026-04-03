package com.niveshmitra.user_profile_service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Integer dependents;
    private Double targetAmount;
    private Integer targetYears;
private String goal;
private String phone;
private String preferredLanguage;
}
