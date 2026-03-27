package com.niveshmitra.risk_profiling_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
public class RiskController {
    @Autowired
    private RiskService riskService;

    @GetMapping("/calculate/{userId}")
    public String calculateRisk(@PathVariable Long userId){
        return riskService.calculateRisk(userId);
    }
}
