package com.niveshmitra.recommendation_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "risk-profiling-service")
public interface RiskClient {
    @GetMapping("/risk/calculate/{userId}")
    String calculateRisk(@PathVariable Long userId);

}
