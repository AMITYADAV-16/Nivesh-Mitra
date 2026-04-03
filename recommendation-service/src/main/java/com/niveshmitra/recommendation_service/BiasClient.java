package com.niveshmitra.recommendation_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "behavioral-bias-service")
public interface BiasClient {
    @PostMapping("/bias/analyze")
    BiasResult analyzeBias(@RequestBody List<String> answers);
}