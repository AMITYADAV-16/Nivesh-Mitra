package com.niveshmitra.recommendation_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "market-data-service")
public interface MarketDataClient {
    @GetMapping("/market/mutual-fund/{schemeCode}")
    MutualFund getMutualFund(@PathVariable String schemeCode);

    @GetMapping("/market/funds/category/{category}")
    List<MutualFund> getFundsByCategory(@PathVariable String category);
}