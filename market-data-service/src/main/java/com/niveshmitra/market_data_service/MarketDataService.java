package com.niveshmitra.market_data_service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MarketDataService {
    private final RestTemplate restTemplate = new RestTemplate();
@Cacheable(value ="mutualFunds" ,key ="#schemeCode")
    public MutualFund getMutualFundData(String schemeCode){
    String url = "https://api.mfapi.in/mf/" + schemeCode + "/latest";
    try{
        Map response = restTemplate.getForObject(url , Map.class);

        Map meta =(Map) response.get("meta");
        List<Map> data = (List<Map>) response.get("data");

        MutualFund fund = new MutualFund();
        fund.setSchemeCode(schemeCode);
        fund.setSchemeName((String) meta.get("scheme_name"));
        fund.setNav((String) data.get(0).get("nav"));
        Map latestData = data.get(0);
        fund.setDate(latestData.containsKey("date") ?
                (String) latestData.get("date") :
                (String) latestData.get("Date"));
        return fund;
    }catch (Exception e){
        throw new RuntimeException("Could not fetch fund data for: "+ schemeCode);
    }
}

    public List<MutualFund> getFundsByCategory(String category) {
        // Map category to better MFAPI search terms with hardcoded popular Indian funds
        Map<String, List<String>> categoryFundCodes = Map.of(
                "small cap", List.of("120505", "125497", "119598", "118989"),  // Nippon, Axis, SBI, HDFC Small Cap
                "large cap", List.of("120503", "118701", "119223", "118989"),  // Nippon, Mirae, Axis, HDFC Large Cap
                "liquid",    List.of("119062", "120594", "118825", "119775")   // SBI, HDFC, Axis, Nippon Liquid
        );

        List<String> codes = categoryFundCodes.getOrDefault(
                category.toLowerCase(),
                categoryFundCodes.get("large cap") // default fallback
        );

        List<MutualFund> funds = new ArrayList<>();
        for (String code : codes) {
            try {
                MutualFund fund = getMutualFundData(code);
                funds.add(fund);
            } catch (Exception e) {
                // skip funds that fail, don't crash the whole list
                System.err.println("Skipping fund " + code + ": " + e.getMessage());
            }
        }
        return funds;
    }
    }

