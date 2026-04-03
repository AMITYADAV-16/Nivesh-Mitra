package com.example.whatsapp_service;


import lombok.Data;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserSession {
    private SessionState state = SessionState.START;
    private String name;
    private Integer age;
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Integer dependents;
    private String goal;
    private Double targetAmount;
    private Integer targetYears;
    private String preferredLanguage;
    private List<String> biasAnswers = new ArrayList<>();
    private Long userId;
    private String phone;
}