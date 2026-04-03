package com.example.whatsapp_service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WhatsappService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String fromNumber;

    @Autowired
    private UserProfileClient userProfileClient;

    @Autowired
    private RecommendationClient recommendationClient;

    private final Map<String, UserSession> sessions = new HashMap<>();

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void handleIncomingMessage(String from, String body) {
        String phone = from.replace("whatsapp:", "").trim();
        String message = body.trim();

        UserSession session = sessions.getOrDefault(phone, new UserSession());
        if (session.getState() == SessionState.DONE) {
            session = new UserSession();
            sessions.put(phone, session);
        }
session.getPhone(phone);
        String response = processMessage(session, message, phone);
        sessions.put(phone, session);
        sendMessage(from, response);
    }

    private String processMessage(UserSession session, String message, String phone) {
        switch (session.getState()) {

            case START:
                session.setState(SessionState.ASKED_NAME);
                return "Hi 👋\n\n" +
                        "Main aapki financial details ke basis par ek simple investment plan banaunga.\n\n" +
                        "2–3 minute lagenge.\n\n" +
                        "Start karte hain — aapka naam kya hai?";

            case ASKED_NAME:
                session.setName(message);
                session.setState(SessionState.ASKED_AGE);
                return String.format(
                        "Theek hai, %s.\n\n" +
                                "Aapki age kya hai?\n" +
                                "(Sirf number likhein, jaise 25)",
                        message
                );
            case ASKED_AGE:
                try {
                    int age = Integer.parseInt(message.trim());
                    session.setAge(age);
                    session.setState(SessionState.ASKED_INCOME);
                    String ageComment = "";

                    return ageComment + "Theek hai.\n\n" +
                            "Aapki monthly income kitni hai? (Rupees mein)\n" +
                            "(Sirf number, jaise 50000)";
                } catch (NumberFormatException e) {
                    return "⚠️ Sirf number likhein please!\nJaise: *25*";
                }

            case ASKED_INCOME:
                try {
                    double income = Double.parseDouble(message.trim());
                    session.setMonthlyIncome(income);
                    session.setState(SessionState.ASKED_EXPENSES);
                    String incomeComment = "";

                    return incomeComment + "Theek hai.\n\n" +
                            "Aapke monthly expenses kitne hain?\n" +
                            "(Rent, khana, travel sab milake, jaise 30000)";
                } catch (NumberFormatException e) {
                    return "Please valid number likhein.\nExample: 50000";
                }

            case ASKED_EXPENSES:
                try {
                    double expenses = Double.parseDouble(message.trim());
                    session.setMonthlyExpenses(expenses);
                    session.setState(SessionState.ASKED_DEPENDENTS);
                    double savings = session.getMonthlyIncome() - expenses;
                    String savingsComment;
                    if (savings <= 0) {
                        savingsComment = "Aapke expenses income se zyada hain, isliye abhi savings nahi ho rahi.";
                    } else if (savings < 5000) {
                        savingsComment = String.format("Aap Rs %.0f/month bacha sakte hain. Thoda kam hai, lekin Rs 500 SIP se bhi shuruat ho sakti hai!", savings);
                    } else if (savings < 20000) {
                        savingsComment = String.format("*Rs %.0f/month savings* — ye toh achha hai! Iske saath hum kuch solid karte hain 💪", savings);
                    } else {
                        savingsComment = String.format("Aap approx Rs %.0f per month save kar rahe hain.", savings);
                    }
                    return savingsComment + "\n\n" +
                            "Aapke kitne dependents hain?\n" +
                            "(Jo log aapki income par depend karte hain)\n" +
                            "Example: 2";
                } catch (NumberFormatException e) {
                    return "Please valid number likhein.\nExample: 30000";
                }

            case ASKED_DEPENDENTS:
                try {
                    session.setDependents(Integer.parseInt(message.trim()));
                    session.setState(SessionState.ASKED_GOAL);
                    return "Samajh gaya! 👍\n\n" +
                            "Aap kisliye invest karna chahte hain?\n\n" +
                            "1️⃣ Retirement \n" +
                            "2️⃣ Bachche ki padhai \n" +
                            "3️⃣ Ghar khareedna\n" +
                            "4️⃣ Wealth banana \n" +
                            "5️⃣ Emergency fund \n\n" +
                            "Number reply karein (1-5)";
                } catch (NumberFormatException e) {
                    return "Please valid number likhein.\nExample: 2";
                }

            case ASKED_GOAL:
                String goal = switch (message.trim()) {
                    case "1" -> "Retirement";
                    case "2" -> "Child Education";
                    case "3" -> "Buy a House";
                    case "4" -> "Wealth Creation";
                    case "5" -> "Emergency Fund";
                    default -> message;
                };
                session.setGoal(goal);
                session.setState(SessionState.ASKED_TARGET_AMOUNT);
                return String.format(
                        "Theek hai — %s.\n\n" +
                                "Is goal ke liye aap kitna amount target kar rahe hain?\n" +
                                "(Example: 5000000 = 50 lakh)",
                        goal
                );
            case ASKED_TARGET_AMOUNT:
                try {
                    double targetAmount = Double.parseDouble(message.trim());
                    session.setTargetAmount(targetAmount);
                    session.setState(SessionState.ASKED_TARGET_YEARS);
                    String amountStr = formatAmount(targetAmount);
                    return String.format(
                            "Theek hai — %s.\n\n" +
                                    "Aap ise kitne saalon mein achieve karna chahte hain?\n" +
                                    "(Sirf number, jaise 10)",
                            amountStr
                    );
                } catch (NumberFormatException e) {
                    return "Please valid number likhein.\nExample: 1000000";
                }

            case ASKED_TARGET_YEARS:
                try {
                    int years = Integer.parseInt(message.trim());
                    session.setTargetYears(years);
                    session.setState(SessionState.ASKED_LANGUAGE);

                    // Calculate required SIP
                    double requiredSip = calculateRequiredSIP(
                            session.getTargetAmount(), years, 0.12);
                    double monthlySavings = session.getMonthlyIncome() - session.getMonthlyExpenses();
                    double sipPercent = (requiredSip / monthlySavings) * 100;

                    String sipFeedback;
                    if (requiredSip <= 0) {
                        sipFeedback = "Is goal ke liye SIP ki zarurat nahi hai.";
                    } else if (requiredSip > monthlySavings) {
                        sipFeedback = String.format(
                                "Is goal ke liye approx Rs %.0f per month SIP chahiye hogi.\n" +
                                        "Ye aapki current savings se zyada hai.", requiredSip);
                    } else {
                        sipFeedback = String.format(
                                "Is goal ke liye approx Rs %.0f per month SIP chahiye hogi.\n" +
                                        "Ye aapki savings ka lagbhag %.0f%% hai.",
                                requiredSip, sipPercent);
                    }

                    return sipFeedback + "\n\n" +
                            "Aap kis *language* mein apna investment plan chahte hain?\n\n" +
                            "1️⃣ English\n" +
                            "2️⃣ Hindi (हिंदी)\n" +
                            "3️⃣ Hinglish";
                } catch (NumberFormatException e) {
                    return "⚠️ Sirf number likhein!\nJaise: *10*";
                }

            case ASKED_LANGUAGE:
                String language = switch (message.trim()) {
                    case "2" -> "HINDI";
                    case "3" -> "HINGLISH";
                    default -> "ENGLISH";
                };
                session.setPreferredLanguage(language);
                session.setState(SessionState.ASKED_BIAS_Q1);
                return "Almost done! 🎯\n\n" +
                        "Ab main aapki *investment personality* samjhunga.\n" +
                        "5 quick questions — har question ka jawab sirf *A, B, C ya D* mein dena hai.\n\n" +
                        getBiasQ1(language);

            case ASKED_BIAS_Q1:
                if (!isValidBiasAnswer(message))
                    return getInvalidAnswerMsg(session.getPreferredLanguage());
                session.getBiasAnswers().add(message.toUpperCase().trim());
                session.setState(SessionState.ASKED_BIAS_Q2);
                return getBiasQ2(session.getPreferredLanguage());

            case ASKED_BIAS_Q2:
                if (!isValidBiasAnswer(message))
                    return getInvalidAnswerMsg(session.getPreferredLanguage());
                session.getBiasAnswers().add(message.toUpperCase().trim());
                session.setState(SessionState.ASKED_BIAS_Q3);
                return getBiasQ3(session.getPreferredLanguage());

            case ASKED_BIAS_Q3:
                if (!isValidBiasAnswer(message))
                    return getInvalidAnswerMsg(session.getPreferredLanguage());
                session.getBiasAnswers().add(message.toUpperCase().trim());
                session.setState(SessionState.ASKED_BIAS_Q4);
                return getBiasQ4(session.getPreferredLanguage());

            case ASKED_BIAS_Q4:
                if (!isValidBiasAnswer(message))
                    return getInvalidAnswerMsg(session.getPreferredLanguage());
                session.getBiasAnswers().add(message.toUpperCase().trim());
                session.setState(SessionState.ASKED_BIAS_Q5);
                return getBiasQ5(session.getPreferredLanguage());

            case ASKED_BIAS_Q5:
                if (!isValidBiasAnswer(message))
                    return getInvalidAnswerMsg(session.getPreferredLanguage());
                session.getBiasAnswers().add(message.toUpperCase().trim());
                session.setState(SessionState.DONE);
                return generateRecommendation(session);
            case ASKED_DID_INVEST: {
                String msg = message.toUpperCase().trim();
                if (msg.equals("YES") || msg.equals("HAAN") || msg.equals("HAN")) {
                    session.setState(SessionState.ASKED_INVEST_AMOUNT);
                    return "💰 Waah! Kitna invest kiya is mahine?\n" +
                            "(How much did you invest? Type amount in ₹)\n\n" +
                            "Example: *5000*";
                } else if (msg.equals("NO") || msg.equals("NAHI") || msg.equals("NHI")) {
                    session.setState(SessionState.DONE);
                    return "No worries! 🤗\n\n" +
                            "Yaad rakhein — SIP ka magic *consistency* mein hai.\n\n" +
                            "Agli baar zaroor karein! Main aapko next month phir yaad dilaunga. 📅\n\n" +
                            "_Hi bhejo naya plan banane ke liye._\n" +
                            "_Nivesh Mitra — aapka financial dost_ 💚";
                } else {
                    return "Please reply *YES* ya *NO* mein 🙏";
                }
            }

            case ASKED_INVEST_AMOUNT: {
                try {
                    double amount = Double.parseDouble(message.replaceAll("[^0-9.]", ""));
                    session.setState(SessionState.DONE);
                    // This will be picked up by tracking-service via its own endpoint
                    // For now, just acknowledge
                    return String.format(
                            "✅ *₹%.0f recorded!*\n\n" +
                                    "Bahut achha! 🎉\n\n" +
                                    "Aap apne goal ki taraf badh rahe hain.\n" +
                                    "Consistency hi sabse badi strategy hai! 💪\n\n" +
                                    "_Hi bhejo naya plan banane ke liye._\n" +
                                    "_Nivesh Mitra — aapka financial dost_ 💚",
                            amount);
                } catch (NumberFormatException e) {
                    return "Please sirf number likhein.\nExample: *5000*";
                }
            }
            default:
                sessions.remove(phone);
                return "Kuch problem aa gayi. *Hi* bhejo dobara shuru karne ke liye!";
        }
    }

    // SIP Calculator — calculates monthly SIP needed to reach target
    // Uses formula: SIP = FV * r / ((1+r)^n - 1)
    // where r = monthly rate, n = months
    private double calculateRequiredSIP(double targetAmount, int years, double annualRate) {
        double monthlyRate = annualRate / 12;
        int months = years * 12;
        double denominator = Math.pow(1 + monthlyRate, months) - 1;
        if (denominator == 0) return targetAmount / months;
        return (targetAmount * monthlyRate) / denominator;
    }

    // Format large numbers nicely
    private String formatAmount(double amount) {
        if (amount >= 10000000) {
            return String.format("Rs %.1f Crore", amount / 10000000);
        } else if (amount >= 100000) {
            return String.format("Rs %.1f Lakh", amount / 100000);
        } else {
            return String.format("Rs %.0f", amount);
        }
    }

    private String generateRecommendation(UserSession session) {
        try {
            UserProfileClient.UserProfile profile = new UserProfileClient.UserProfile();
            profile.setName(session.getName());
            profile.setAge(session.getAge());
            profile.setMonthlyIncome(session.getMonthlyIncome());
            profile.setMonthlyExpenses(session.getMonthlyExpenses());
            profile.setDependents(session.getDependents());
            profile.setGoal(session.getGoal());
            profile.setPreferredLanguage(session.getPreferredLanguage());
            profile.setTargetAmount(session.getTargetAmount());   // ← NEW
            profile.setTargetYears(session.getTargetYears());     // ← NEW
profile.setPhone(session.getPhone());
            UserProfileClient.UserProfile saved = userProfileClient.createUser(profile);
            session.setUserId(saved.getId());

            String recommendation = recommendationClient.getRecommendation(
                    saved.getId(),
                    session.getBiasAnswers()
            );

            return formatFinalResponse(session, recommendation);

        } catch (Exception e) {
            return "⚠️ Kuch problem aa gayi plan banane mein. *Hi* bhejo dobara try karne ke liye.";
        }
    }

    private String formatFinalResponse(UserSession session, String recommendation) {
        String targetStr = formatAmount(session.getTargetAmount());
        double requiredSip = calculateRequiredSIP(
                session.getTargetAmount(), session.getTargetYears(), 0.12);

        String header = String.format(
                "📊 %s apka Investment Plan\n\n" +
                        "━━━━━━━━━━━━━━━━━\n\n" +

                        "🎯 Goal: %s\n" +
                        "💰 Target: %s\n" +
                        "⏳ Time: %d years\n\n" +

                        "📈 Required SIP: Rs %,.0f/month\n\n" +

                        "━━━━━━━━━━━━━━━━━\n\n" +

                        "⚠️ Feasibility Check\n" +
                        "%s\n\n" +

                        "━━━━━━━━━━━━━━━━━\n\n" +

                        "📦 Suggested Plan\n%s\n\n" +

                        "━━━━━━━━━━━━━━━━━\n\n" +

                        "🛡️ Safety\n" +
                        "3–6 months expenses ka emergency fund maintain karein.\n\n" +

                        "━━━━━━━━━━━━━━━━━",
                session.getName(),
                session.getGoal(),
                targetStr,
                session.getTargetYears(),
                requiredSip
        );

        String footer = "\n\n━━━━━━━━━━━━━━━━━\n" +
                "💡 _Hi bhejo naya plan banane ke liye_\n" +
                "   _Nivesh Mitra — aapka financial dost_";

        return header + recommendation + footer;
    }

    private String getBiasQ1(String lang) {
        return switch (lang) {
            case "HINDI" -> "*Q1:* आपने Mutual Fund में Rs 10,000 लगाए और वो 20% गिर गया। आप क्या करेंगे?\n\n" +
                    "A) तुरंत बेच दो\nB) रुको और देखो\nC) और खरीदो\nD) दोस्तों से पूछो\n\n_A, B, C या D लिखें_";
            case "HINGLISH" -> "*Q1:* Aapne Rs 10,000 Mutual Fund mein lagaye aur wo 20% gir gaya. Aap kya karenge?\n\n" +
                    "A) Turant bech do\nB) Ruko aur dekho\nC) Aur kharido\nD) Doston se pucho\n\n_A, B, C ya D_";
            default -> "*Q1:* You invested Rs 10,000 in a mutual fund and it drops 20%. What do you do?\n\n" +
                    "A) Sell immediately\nB) Hold and wait\nC) Buy more\nD) Ask friends/family\n\n_Reply A, B, C or D_";
        };
    }

    private String getBiasQ2(String lang) {
        return switch (lang) {
            case "HINDI" -> "*Q2:* आपके colleague ने कहा सब crypto खरीद रहे हैं। आप:\n\n" +
                    "A) तुरंत invest करें\nB) पहले research करें\nC) ignore करें\nD) और बढ़े तो invest करें\n\n_A, B, C या D_";
            case "HINGLISH" -> "*Q2:* Colleague ne kaha sab crypto khareed rahe hain. Aap:\n\n" +
                    "A) Turant invest karo\nB) Pehle research karo\nC) Ignore karo\nD) Aur badhe toh invest karo\n\n_A, B, C ya D_";
            default -> "*Q2:* Your colleague says everyone is buying crypto. You:\n\n" +
                    "A) Invest immediately\nB) Research first\nC) Ignore it\nD) Wait to see if it rises\n\n_Reply A, B, C or D_";
        };
    }

    private String getBiasQ3(String lang) {
        return switch (lang) {
            case "HINDI" -> "*Q3:* पिछले महीने market गिरा। अगले महीने:\n\n" +
                    "A) फिर गिरेगा\nB) ठीक होगा\nC) Same रहेगा\nD) कह नहीं सकते\n\n_A, B, C या D_";
            case "HINGLISH" -> "*Q3:* Pichle mahine market gira. Agle mahine:\n\n" +
                    "A) Phir girega\nB) Theek hoga\nC) Same rahega\nD) Keh nahi sakte\n\n_A, B, C ya D_";
            default -> "*Q3:* The market crashed last month. Next month it will:\n\n" +
                    "A) Crash again\nB) Recover\nC) Stay the same\nD) Impossible to predict\n\n_Reply A, B, C or D_";
        };
    }

    private String getBiasQ4(String lang) {
        return switch (lang) {
            case "HINDI" -> "*Q4:* Rs 50,000 invest करने हैं। आप:\n\n" +
                    "A) Savings account में रखें\nB) FD, MF, Gold में बांटें\nC) एक stock में सब लगाएं\nD) रिश्तेदारों जैसा करें\n\n_A, B, C या D_";
            case "HINGLISH" -> "*Q4:* Rs 50,000 invest karne hain. Aap:\n\n" +
                    "A) Savings mein rakho\nB) FD, MF, Gold mein baanto\nC) Ek stock mein sab\nD) Rishtedaroon jaisa\n\n_A, B, C ya D_";
            default -> "*Q4:* You have Rs 50,000 to invest. You:\n\n" +
                    "A) Keep in savings\nB) Split across FD, MF, Gold\nC) Put all in one stock\nD) Do what relatives did\n\n_Reply A, B, C or D_";
        };
    }

    private String getBiasQ5(String lang) {
        return switch (lang) {
            case "HINDI" -> "*Q5:* Portfolio कितनी बार check करते हैं?\n\n" +
                    "A) दिन में कई बार\nB) महीने में एक बार\nC) कभी-कभी\nD) जब कोई कहे\n\n_A, B, C या D_";
            case "HINGLISH" -> "*Q5:* Portfolio kitni baar check karte ho?\n\n" +
                    "A) Din mein kai baar\nB) Mahine mein ek baar\nC) Kabhi kabhi\nD) Jab koi kahe\n\n_A, B, C ya D_";
            default -> "*Q5:* How often do you check your portfolio?\n\n" +
                    "A) Multiple times a day\nB) Once a month\nC) Rarely\nD) Only when told\n\n_Reply A, B, C or D_";
        };
    }

    private String getInvalidAnswerMsg(String lang) {
        return switch (lang) {
            case "HINDI" -> "⚠️ कृपया A, B, C या D लिखें।";
            case "HINGLISH" -> "⚠️ A, B, C ya D mein se ek likhein.";
            default -> "⚠️ Please reply with A, B, C or D only.";
        };
    }

    private boolean isValidBiasAnswer(String message) {
        String m = message.toUpperCase().trim();
        return m.equals("A") || m.equals("B") || m.equals("C") || m.equals("D");
    }

    private void sendMessage(String to, String body) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber("whatsapp:" + fromNumber),
                body
        ).create();
    }
}