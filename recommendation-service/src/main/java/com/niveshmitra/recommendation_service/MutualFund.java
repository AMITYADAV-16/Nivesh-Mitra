package com.niveshmitra.recommendation_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund {
    private String schemeCode;
    private String schemeName;
    private String nav;
    private String date;
}