package com.niveshmitra.recommendation_service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MarketDataClient marketDataClient;

    @Autowired
    private RiskClient riskClient;

    @Autowired
    private ChatClient chatClient;

    public String getRecommendation(Long userId) {

        // fetch user profile
        User user = userClient.getUserById(userId);

        // fetch some popular Indian mutual funds
    String riskBand = riskClient.calculateRisk(userId);
    String category;
    if(riskBand.equals("AGGRESSIVE")){
        category = "small cap";
    }else if(riskBand.equals("MODERATE")){
        category ="large cap";
    }else {
        category = "liquid";
    }
        List<MutualFund> funds = marketDataClient.getFundsByCategory(category);

        StringBuilder fundInfo = new StringBuilder();
        for(MutualFund f : funds){
            fundInfo.append("- ").append(f.getSchemeName()).append("\n");
        }
        // calculate savings
        double savings = user.getMonthlyIncome() - user.getMonthlyExpenses();

        String language = (user.getPreferredLanguage() != null && !user.getPreferredLanguage().isBlank())
                ? user.getPreferredLanguage().toUpperCase()
                : "ENGLISH";
        String languageInstruction = switch (language){
            case "HINDI"  -> "Give the personalized investment plan completely in clear, conversational Hindi using the Devanagari script. Keep financial terms like 'Mutual Fund', 'SIP', and 'NAV' in English.";
            case "HINGLISH" -> "Give the personalized investment plan completely in Hinglish (Hindi language written in the English alphabet, e.g., 'Aapko apna paisa invest karna chahiye').";
            default -> "Give the personalized investment plan in simple English.";
        };

        // build the prompt
        String prompt = String.format("""
                        You are a friendly Indian financial advisor helping middle-class Indians invest wisely.
                        
                                       User profile:
                                       - Name: %s
                                       - Age: %d years
                                       - Monthly income: Rs %.0f
                                       - Monthly expenses: Rs %.0f
                                       - Monthly savings: Rs %.0f
                                       - Dependents: %d
                                       - Goal: %s
                                       - Risk profile: %s
                        
                                       Available mutual funds for this user's risk profile:
                                       %s
                        
                                       Give a personalized investment plan in simple English.
                                       Pick 2-3 funds from the list above and suggest specific SIP amounts.
                                       Also mention PPF and emergency fund if relevant.
                                       Keep response under 200 words.
                """,
                user.getName(), user.getAge(),
                user.getMonthlyIncome(), user.getMonthlyExpenses(), savings,
                user.getDependents(), user.getGoal(), riskBand,
                fundInfo.toString(),
                languageInstruction
        );

        // call the AI
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
