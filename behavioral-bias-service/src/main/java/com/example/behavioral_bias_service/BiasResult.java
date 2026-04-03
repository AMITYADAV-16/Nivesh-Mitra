package com.example.behavioral_bias_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiasResult {
    private BiasType dominantBias;
    private String biasDescription;
    private String impactOnWealth;
    private String recommendation;
}
