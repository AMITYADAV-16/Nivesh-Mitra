package com.example.whatsapp_service;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationFeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {
        // 10 seconds connect timeout, 90 seconds read timeout
        // Groq LLM API can take 20-30s; recommendation-service may retry up to 2 times.
        // 90s gives enough headroom: 2 attempts × 30s + waits ≈ 65s worst case.
        return new Request.Options(10_000, 90_000);
    }

    @Bean
    public Retryer retryer() {
        // Avoid unsafe retries for POST requests because they may be non-idempotent
        return Retryer.NEVER_RETRY;
    }
}
