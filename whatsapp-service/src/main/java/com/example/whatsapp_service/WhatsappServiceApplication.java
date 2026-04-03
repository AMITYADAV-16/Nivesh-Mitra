package com.example.whatsapp_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WhatsappServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsappServiceApplication.class, args);
	}

}
