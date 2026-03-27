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
    String url = "https://api.mfapi.in/mf/search?q={category}" + category;
    try{
        List<Map> response =restTemplate.getForObject(url, List.class, category);
        List<MutualFund> funds = new ArrayList<>();
        for(int i =0; i< Math.min(4 , response.size()); i++){
            Map fund = response.get(i);
            String code = String.valueOf(fund.get("schemeCode"));
            String name = String.valueOf(fund.get("schemeName"));
            MutualFund mf = new MutualFund();
            mf.setSchemeCode(code);
            mf.setSchemeName(name);
            funds.add(mf);
        }
        return funds;
    }catch (Exception e){
        throw new RuntimeException("Could not fetch funds for: "+ category, e);
    }
    }
}
