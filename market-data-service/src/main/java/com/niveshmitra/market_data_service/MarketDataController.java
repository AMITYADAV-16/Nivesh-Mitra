package com.niveshmitra.market_data_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/market")
public class MarketDataController {

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/mutual-fund/{schemeCode}")
    public MutualFund getMutualFund(@PathVariable String schemeCode) {
        return marketDataService.getMutualFundData(schemeCode);
    }
    @GetMapping("/funds/category/{category}")
    public List<MutualFund> getFundsByCategory(@PathVariable String category) {
        return marketDataService.getFundsByCategory(category);
    }
}