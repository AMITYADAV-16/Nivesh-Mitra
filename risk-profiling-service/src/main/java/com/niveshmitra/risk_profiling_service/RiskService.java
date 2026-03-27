package com.niveshmitra.risk_profiling_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiskService {
    @Autowired
    private UserClient userClient;

    public String calculateRisk(Long userId){
        User user = userClient.getUserById(userId);
        int score =0;

        if(user.getAge() < 30) score += 40;
        else if(user.getAge() < 45) score += 25;
        else score += 10;

        double saving = user.getMonthlyIncome() - user.getMonthlyExpenses();
        double savingRate = saving / user.getMonthlyIncome();
        if(savingRate > 0.4) score +=30;
        else if (savingRate > 0.2) score += 20;
        else score += 5;

        if(user.getDependents() == 0) score += 30;
        else if (user.getDependents() <= 2) score += 15;
        else score += 5;

        if (score >= 70) return "AGGRESSIVE";
        else if (score >= 40) return "MODERATE";
        else return "CONSERVATIVE";
    }
}
