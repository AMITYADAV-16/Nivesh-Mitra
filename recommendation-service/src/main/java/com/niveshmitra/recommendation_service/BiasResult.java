package com.niveshmitra.recommendation_service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiasResult {
    private String dominantBias;
    private String biasDescription;
    private String impactOnWealth;
    private String recommendation;
}