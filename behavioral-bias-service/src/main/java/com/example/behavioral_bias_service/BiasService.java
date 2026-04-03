package com.example.behavioral_bias_service;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiasService {

    // The 5 psychological questions
    public List<String> getQuestions() {
        return List.of(
                "Q1: You invested Rs 10,000 in a mutual fund and it drops 20%. What do you do?\n" +
                        "A) Sell immediately to stop further loss\n" +
                        "B) Hold and wait\n" +
                        "C) Buy more at the lower price\n" +
                        "D) Ask friends/family what they are doing",

                "Q2: Your colleague tells you everyone is buying crypto right now. You:\n" +
                        "A) Immediately invest too — if everyone is doing it, it must be right\n" +
                        "B) Research first before deciding\n" +
                        "C) Ignore it completely\n" +
                        "D) Wait to see if it keeps going up, then invest",

                "Q3: The stock market crashed last month. You think next month it will:\n" +
                        "A) Crash again — it crashed last month so it will crash again\n" +
                        "B) Recover — markets always recover long term\n" +
                        "C) Stay the same\n" +
                        "D) Impossible to predict",

                "Q4: You have Rs 50,000 to invest. You:\n" +
                        "A) Keep it in a savings account — investing feels too risky\n" +
                        "B) Split it across FD, mutual funds and gold\n" +
                        "C) Put it all in one stock you're confident about\n" +
                        "D) Do what your relatives did with their money",

                "Q5: How often do you check your investment portfolio?\n" +
                        "A) Multiple times a day — I panic if it goes red\n" +
                        "B) Once a month\n" +
                        "C) Rarely — I invest and forget\n" +
                        "D) Only when someone tells me to check"
        );
    }

    // answers is a list of 5 answers: "A", "B", "C", or "D"
    public BiasResult analyzeBias(List<String> answers) {

        // Score map for each bias
        Map<BiasType, Integer> scores = new HashMap<>();
        for (BiasType bias : BiasType.values()) {
            scores.put(bias, 0);
        }

        // Q1: Loss aversion = A, Herd mentality = D
        String a1 = answers.get(0).toUpperCase().trim();
        if (a1.equals("A")) scores.merge(BiasType.LOSS_AVERSION, 2, Integer::sum);
        if (a1.equals("D")) scores.merge(BiasType.HERD_MENTALITY, 2, Integer::sum);

        // Q2: Herd mentality = A, Recency bias = D
        String a2 = answers.get(1).toUpperCase().trim();
        if (a2.equals("A")) scores.merge(BiasType.HERD_MENTALITY, 2, Integer::sum);
        if (a2.equals("D")) scores.merge(BiasType.RECENCY_BIAS, 2, Integer::sum);

        // Q3: Recency bias = A, Overconfidence = C (thinking they know)
        String a3 = answers.get(2).toUpperCase().trim();
        if (a3.equals("A")) scores.merge(BiasType.RECENCY_BIAS, 2, Integer::sum);
        if (a3.equals("C")) scores.merge(BiasType.OVERCONFIDENCE, 1, Integer::sum);

        // Q4: Status quo = A, Overconfidence = C, Herd mentality = D
        String a4 = answers.get(3).toUpperCase().trim();
        if (a4.equals("A")) scores.merge(BiasType.STATUS_QUO, 2, Integer::sum);
        if (a4.equals("C")) scores.merge(BiasType.OVERCONFIDENCE, 2, Integer::sum);
        if (a4.equals("D")) scores.merge(BiasType.HERD_MENTALITY, 2, Integer::sum);

        // Q5: Loss aversion = A, Status quo = D
        String a5 = answers.get(4).toUpperCase().trim();
        if (a5.equals("A")) scores.merge(BiasType.LOSS_AVERSION, 2, Integer::sum);
        if (a5.equals("D")) scores.merge(BiasType.STATUS_QUO, 2, Integer::sum);

        // find dominant bias
        BiasType dominantBias = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(BiasType.STATUS_QUO);

        return buildResult(dominantBias);
    }

    private BiasResult buildResult(BiasType bias) {
        return switch (bias) {
            case LOSS_AVERSION -> new BiasResult(
                    BiasType.LOSS_AVERSION,
                    "You fear losses more than you value gains. This makes you sell investments too early when markets dip.",
                    "You may be losing 4-6% annual returns by exiting mutual funds during market corrections instead of staying invested.",
                    "Start a SIP — it removes emotion from investing. You invest the same amount every month regardless of market mood."
            );
            case HERD_MENTALITY -> new BiasResult(
                    BiasType.HERD_MENTALITY,
                    "You tend to follow what others are doing with money — relatives, friends, or social media trends.",
                    "Following the crowd often means buying at the top and selling at the bottom, destroying wealth over time.",
                    "Create a personal investment plan based on YOUR goals and income, not what others are doing."
            );
            case RECENCY_BIAS -> new BiasResult(
                    BiasType.RECENCY_BIAS,
                    "You give too much weight to recent events. If markets crashed last month, you assume they will crash again.",
                    "Recency bias causes you to miss recoveries. Most wealth is built by staying invested through crashes.",
                    "Look at 10-year mutual fund returns, not last month's performance. Time in market beats timing the market."
            );
            case OVERCONFIDENCE -> new BiasResult(
                    BiasType.OVERCONFIDENCE,
                    "You tend to be overconfident about your investment picks and may take concentrated bets.",
                    "Overconfidence leads to under-diversification. One bad stock pick can wipe out years of savings.",
                    "Diversify across mutual funds, gold, and FDs. No single investment should be more than 20% of your portfolio."
            );
            case STATUS_QUO -> new BiasResult(
                    BiasType.STATUS_QUO,
                    "You prefer to keep money in savings accounts or FDs because investing feels uncomfortable or risky.",
                    "With 6-7% inflation in India, keeping money in savings accounts actually loses value every year.",
                    "Start with Rs 500/month SIP in a large-cap index fund. Small steps beat doing nothing."
            );
        };
    }
}