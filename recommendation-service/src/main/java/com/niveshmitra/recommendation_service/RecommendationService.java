package com.niveshmitra.recommendation_service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    @Autowired private UserClient userClient;
    @Autowired private MarketDataClient marketDataClient;
    @Autowired private RiskClient riskClient;
    @Autowired private BiasClient biasClient;
    @Autowired private ChatClient chatClient;

    public String getRecommendation(Long userId, List<String> biasAnswers) {

        User user;
        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            return "Sorry, could not fetch your profile. Please try again later.";
        }
        if (user == null) return "User not found with ID: " + userId;

        String riskBand;
        try {
            riskBand = riskClient.calculateRisk(userId);
        } catch (Exception e) {
            riskBand = "MODERATE";
        }

        String category = switch (riskBand) {
            case "AGGRESSIVE" -> "small cap";
            case "MODERATE"   -> "large cap";
            default           -> "liquid";
        };

        List<MutualFund> funds;
        try {
            funds = marketDataClient.getFundsByCategory(category);
        } catch (Exception e) {
            funds = List.of();
        }

        // Pick top 3 funds max
        List<MutualFund> topFunds = funds != null
                ? funds.stream().limit(3).toList()
                : List.of();

        double savings = user.getMonthlyIncome() - user.getMonthlyExpenses();

        // Bias detection
        String biasName = "";
        String biasTip = "";
        if (biasAnswers != null && biasAnswers.size() == 5) {
            try {
                BiasResult bias = biasClient.analyzeBias(biasAnswers);
                biasName = bias.getDominantBias();
                biasTip  = bias.getRecommendation();
            } catch (Exception ignored) {}
        }

        // SIP math
        double requiredSip = 0, startSip = 0, projectedValue = 0;
        int targetYears = 0;
        double targetAmount = 0;
        if (user.getTargetAmount() != null && user.getTargetYears() != null
                && user.getTargetAmount() > 0 && user.getTargetYears() > 0) {
            targetAmount = user.getTargetAmount();
            targetYears  = user.getTargetYears();
            requiredSip  = calculateRequiredSIP(targetAmount, targetYears, 0.12);
            startSip     = Math.min(requiredSip, savings * 0.5);
            if (startSip < 500) startSip = 500;
            projectedValue = calculateFutureValue(startSip, targetYears, 0.12);
        }

        // Language
        String language = (user.getPreferredLanguage() != null
                && !user.getPreferredLanguage().isBlank())
                ? user.getPreferredLanguage().toUpperCase() : "ENGLISH";

        String langInstruction = switch (language) {
            case "HINDI" ->
                    "Reply ONLY in Hindi (Devanagari script). Keep 'SIP', 'Mutual Fund', 'CAGR' in English.";
            case "HINGLISH" ->
                    "Reply ONLY in Hinglish (Hindi words written in English letters, like 'Aapko invest karna chahiye').";
            default -> "Reply in simple, friendly English.";
        };

        // Fund list for prompt
        StringBuilder fundNames = new StringBuilder();
        for (int i = 0; i < topFunds.size(); i++) {
            fundNames.append(i + 1).append(". ")
                    .append(topFunds.get(i).getSchemeName()).append("\n");
        }

        // Build fund allocation prompt section
        String allocationPrompt = topFunds.size() >= 2
                ? String.format("""
                These are the ONLY funds to recommend (use ALL of them):
                %s
                
                For each fund suggest:
                - Allocation percentage (must sum to 100%%)
                - Monthly SIP amount in Rs (based on starting SIP of Rs %.0f/month)
                - One sentence: why this fund suits this user
                
                Format EXACTLY like this for each fund (no extra lines):
                FUND: <fund name>
                ALLOC: <percentage>%%
                AMOUNT: Rs <amount>/month
                WHY: <one sentence reason>
                """, fundNames, startSip)
                : "Suggest 2-3 suitable funds with allocation percentages summing to 100%.";

        // Bias section
        String biasPrompt = !biasName.isBlank()
                ? String.format("""
                User has '%s' behavioral bias.
                Write ONE punchy line (max 15 words) warning them about this bias.
                Format: BIAS_WARNING: <your line>
                """, biasName)
                : "";

        // Step-up projection
        double year3Sip = startSip * Math.pow(1.10, 2);
        double year5Sip = startSip * Math.pow(1.10, 4);

        String prompt = String.format("""
You are Nivesh Mitra — a sharp, friendly Indian financial advisor on WhatsApp.
You are talking to %s, age %d.

USER PROFILE:
- Monthly income: Rs %.0f
- Monthly savings: Rs %.0f
- Goal: %s in %d years
- Target: %s
- Required SIP: Rs %.0f/month
- Suggested starting SIP: Rs %.0f/month
- Risk level: %s

%s

%s

Write a FEASIBILITY CHECK (2-3 lines max):
- Is Rs %.0f/month achievable given Rs %.0f savings?
- If not, acknowledge it and say starting with Rs %.0f/month is smart
- Mention: with 10%% yearly step-up, Year 3 SIP = Rs %.0f, Year 5 = Rs %.0f
- Projected corpus at starting SIP: %s (mention this number)
Format: FEASIBILITY: <your lines>

Language: %s
Keep EVERY section SHORT. WhatsApp messages — not essays.
""",
                user.getName(), user.getAge(),
                user.getMonthlyIncome(), savings,
                user.getGoal(), targetYears,
                formatAmount(targetAmount),
                requiredSip, startSip,
                riskBand,
                allocationPrompt,
                biasPrompt,
                requiredSip, savings, startSip,
                year3Sip, year5Sip,
                formatAmount(projectedValue),
                langInstruction
        );

        String aiResponse;
        try {
            aiResponse = chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            aiResponse = "";
        }

        // Parse AI response and build final formatted message
        return buildFormattedPlan(user, riskBand, biasName, biasTip,
                topFunds, startSip, requiredSip, targetAmount, targetYears,
                projectedValue, year3Sip, year5Sip, savings, aiResponse, language);
    }

    private String buildFormattedPlan(
            User user, String riskBand, String biasName, String biasTip,
            List<MutualFund> funds, double startSip, double requiredSip,
            double targetAmount, int targetYears, double projectedValue,
            double year3Sip, double year5Sip, double savings,
            String aiResponse, String language) {

        StringBuilder sb = new StringBuilder();

        // ── Header ──────────────────────────────────────────────
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("📊 *").append(user.getName()).append(" ka Investment Plan*\n");
        sb.append("━━━━━━━━━━━━━━━━━\n\n");

        sb.append("🎯 *Goal:* ").append(user.getGoal()).append("\n");
        sb.append("💰 *Target:* ").append(formatAmount(targetAmount))
                .append(" in ").append(targetYears).append(" years\n");
        sb.append("📈 *Required SIP:* Rs ").append(String.format("%,.0f", requiredSip))
                .append("/month\n\n");

        // ── Feasibility ─────────────────────────────────────────
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("⚠️ *Reality Check*\n");

        String feasibility = extractSection(aiResponse, "FEASIBILITY:");
        if (!feasibility.isBlank()) {
            sb.append(feasibility).append("\n\n");
        } else {
            // Fallback if AI didn't follow format
            if (requiredSip > savings) {
                sb.append("Required SIP Rs ").append(String.format("%,.0f", requiredSip))
                        .append(" exceeds your savings.\n");
                sb.append("Smart move: Start with *Rs ").append(String.format("%,.0f", startSip))
                        .append("/month* and step up 10% yearly. 💪\n\n");
            } else {
                sb.append("Your savings can cover the full SIP. You're good to go! ✅\n\n");
            }
        }

        // ── Step-up plan ────────────────────────────────────────
        sb.append("📅 *Step-Up Plan (10% yearly)*\n");
        sb.append("Year 1: *Rs ").append(String.format("%,.0f", startSip)).append("*");
        sb.append(" → Year 3: *Rs ").append(String.format("%,.0f", year3Sip)).append("*");
        sb.append(" → Year 5: *Rs ").append(String.format("%,.0f", year5Sip)).append("*\n");
        sb.append("Projected corpus: *").append(formatAmount(projectedValue))
                .append("* at 12% CAGR\n\n");

        // ── Fund Allocation ─────────────────────────────────────
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("📦 *Your ").append(funds.size()).append("-Fund Plan*\n\n");

        String[] emojis = {"1️⃣", "2️⃣", "3️⃣"};
        String[] fundBlocks = extractFundBlocks(aiResponse, funds.size());

        for (int i = 0; i < funds.size(); i++) {
            sb.append(emojis[i]).append(" *").append(funds.get(i).getSchemeName()).append("*\n");
            if (fundBlocks[i] != null && !fundBlocks[i].isBlank()) {
                sb.append(fundBlocks[i]).append("\n");
            }
            sb.append("\n");
        }

        // ── Bias warning ────────────────────────────────────────
        if (!biasName.isBlank()) {
            sb.append("━━━━━━━━━━━━━━━━━\n");
            sb.append("🧠 *Your Investor Personality*\n");
            String biasWarning = extractSection(aiResponse, "BIAS_WARNING:");
            if (!biasWarning.isBlank()) {
                sb.append(biasWarning).append("\n");
            } else {
                sb.append("Bias detected: *").append(biasName).append("*\n");
                if (!biasTip.isBlank()) sb.append(biasTip).append("\n");
            }
            sb.append("\n");
        }

        // ── Safety net ──────────────────────────────────────────
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("🛡️ *Safety First*\n");
        double emergencyFund = user.getMonthlyExpenses() * 6;
        sb.append("Keep *").append(formatAmount(emergencyFund))
                .append("* as emergency fund (6 months expenses).\n");
        sb.append("Invest only what's left after this.\n\n");

        // ── Footer ──────────────────────────────────────────────
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("💡 _Hi bhejo naya plan banane ke liye_\n");
        sb.append("   _Nivesh Mitra — aapka financial dost_ 💚");

        return sb.toString();
    }

    // Extract a labelled section from AI response e.g. "FEASIBILITY: ..."
    private String extractSection(String response, String label) {
        if (response == null || response.isBlank()) return "";
        int idx = response.indexOf(label);
        if (idx == -1) return "";
        String after = response.substring(idx + label.length()).trim();
        // Take until next ALL_CAPS: label or end
        int nextLabel = after.indexOf("\nFUND:");
        if (nextLabel == -1) nextLabel = after.indexOf("\nBIAS_WARNING:");
        if (nextLabel == -1) nextLabel = after.indexOf("\nFEASIBILITY:");
        return nextLabel > 0 ? after.substring(0, nextLabel).trim() : after.trim();
    }

    // Extract per-fund blocks from AI response
    private String[] extractFundBlocks(String response, int count) {
        String[] result = new String[count];
        if (response == null || response.isBlank()) return result;

        String[] fundSections = response.split("FUND:");
        for (int i = 0; i < count && i + 1 < fundSections.length; i++) {
            String block = fundSections[i + 1];
            StringBuilder formatted = new StringBuilder();

            String alloc  = extractInlineValue(block, "ALLOC:");
            String amount = extractInlineValue(block, "AMOUNT:");
            String why    = extractInlineValue(block, "WHY:");

            if (!alloc.isBlank())  formatted.append("   ").append(alloc).append(" allocation\n");
            if (!amount.isBlank()) formatted.append("   ").append(amount).append("\n");
            if (!why.isBlank())    formatted.append("   _").append(why).append("_");

            result[i] = formatted.toString();
        }
        return result;
    }

    private String extractInlineValue(String block, String key) {
        int idx = block.indexOf(key);
        if (idx == -1) return "";
        String after = block.substring(idx + key.length()).trim();
        int newline = after.indexOf('\n');
        return newline > 0 ? after.substring(0, newline).trim() : after.trim();
    }

    private double calculateRequiredSIP(double target, int years, double rate) {
        if (years <= 0) return target;
        double r = rate / 12;
        int n = years * 12;
        double denom = Math.pow(1 + r, n) - 1;
        return denom == 0 ? target / n : (target * r) / denom;
    }

    private double calculateFutureValue(double sip, int years, double rate) {
        double r = rate / 12;
        int n = years * 12;
        return sip * (Math.pow(1 + r, n) - 1) / r;
    }

    private String formatAmount(double amount) {
        if (amount >= 10000000) return String.format("Rs %.1f Crore", amount / 10000000);
        if (amount >= 100000)   return String.format("Rs %.1f Lakh", amount / 100000);
        return String.format("Rs %.0f", amount);
    }
}