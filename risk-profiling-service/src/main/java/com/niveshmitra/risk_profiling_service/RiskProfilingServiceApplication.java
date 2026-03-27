package com.niveshmitra.risk_profiling_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RiskProfilingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiskProfilingServiceApplication.class, args);
	}

}
