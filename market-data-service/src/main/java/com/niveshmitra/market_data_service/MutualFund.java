package com.niveshmitra.market_data_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund implements Serializable {
    private String schemeCode;
    private String schemeName;
    private String nav;
    private String date;
}
