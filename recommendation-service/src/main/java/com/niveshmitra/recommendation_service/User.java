package com.niveshmitra.recommendation_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
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
}