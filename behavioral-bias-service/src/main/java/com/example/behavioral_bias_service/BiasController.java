package com.example.behavioral_bias_service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bias")
public class BiasController {

    @Autowired
    private BiasService biasService;

    // Get the 5 questions
    @GetMapping("/questions")
    public List<String> getQuestions() {
        return biasService.getQuestions();
    }

    // Submit answers and get bias result
    // POST /bias/analyze
    // Body: ["A", "D", "B", "A", "C"]
    @PostMapping("/analyze")
    public BiasResult analyzeBias(@RequestBody List<String> answers) {
        if (answers == null || answers.size() != 5) {
            throw new IllegalArgumentException("Exactly 5 answers required (A, B, C, or D for each question)");
        }
        return biasService.analyzeBias(answers);
    }
}