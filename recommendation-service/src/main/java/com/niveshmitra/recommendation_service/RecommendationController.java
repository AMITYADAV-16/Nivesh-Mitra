package com.niveshmitra.recommendation_service;

import lombok.Lombok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/{userId}")
    public String getRecommendation(@PathVariable Long userId, @RequestBody(required = false) List<String> biasAnswers ){
        return recommendationService.getRecommendation(userId , biasAnswers);
    }
    @GetMapping("/{userId}")
    public String getRecommendation(@PathVariable Long userId) {
        return recommendationService.getRecommendation(userId, null);
    }
}
