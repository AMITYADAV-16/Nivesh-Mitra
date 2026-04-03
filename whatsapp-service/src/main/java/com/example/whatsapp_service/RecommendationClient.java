package com.example.whatsapp_service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "recommendation-service")
public interface RecommendationClient {

    @PostMapping("/recommend/{userId}")
    String getRecommendation(@PathVariable Long userId,
                             @RequestBody List<String> biasAnswers);
}