package com.example.whatsapp_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp")
public class WhatsappController {

    @Autowired
    private WhatsappService whatsappService;

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String receiveMessage(
            @RequestParam("From") String from,
            @RequestParam("Body") String body) {
        whatsappService.handleIncomingMessage(from, body);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>";
    }
}