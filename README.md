# 🤖 Nivesh Mitra — AI WhatsApp Investment Bot

> Personalized investment advisor on WhatsApp for Indian users,
> powered by LLaMA 3.3 70B AI and built with Spring Boot microservices.

---

## 💬 Try it Live

**WhatsApp the bot right now:**
1. Save this number: **+1 415 523 8886**
2. Send: `join <your-sandbox-word>`
3. Then send: `Hi`

> The bot will ask your income, goals, and risk profile —
> and give you a personalized SIP investment plan in 3 minutes.

---

## 🏗️ Architecture
WhatsApp User
│
▼
Twilio API
│
▼
API Gateway (8082)          ← Single entry point
│
├──► whatsapp-service (8087)      ← Conversation flow
│         │
│         ├──► user-profile-service (8081)    ← Saves user data
│         ├──► recommendation-service (8085)  ← AI plan generation
│         ├──► risk-profiling-service (8083)  ← Risk calculation
│         ├──► market-data-service (8084)     ← Live fund data
│         ├──► behavioral-bias-service (8086) ← Bias detection
│
└── Netflix Eureka (8761)         ← Service discovery

---

## ✨ Features

- **AI-Powered Plans** — Groq AI (LLaMA 3.3 70B) generates personalized
  investment plans based on income, goals, and risk profile
- **Behavioral Bias Detection** — 5-question quiz detects loss aversion,
  herd mentality, recency bias, overconfidence, status quo bias
- **SIP Calculator** — Calculates exact monthly SIP needed to reach
  target amount in target years with step-up projections
- **Multilingual** — English, Hindi (हिंदी), Hinglish support
- **Conversational Flow** — Natural WhatsApp conversation, not a form
- **Real Fund Data** — Suggests actual Indian mutual funds by risk profile

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Microservices | Spring Cloud 2025.0.1 |
| Service Discovery | Netflix Eureka |
| Inter-service calls | OpenFeign |
| AI | Groq API — LLaMA 3.3 70B via Spring AI |
| Database | PostgreSQL 15 |
| Caching | Redis 7 |
| Messaging | Twilio WhatsApp API |
| Containerization | Docker + Docker Compose |
| API Gateway | Spring Cloud Gateway |

---

## 🚀 Run Locally

### Prerequisites
- Java 21
- Docker + Docker Compose
- Twilio account (free sandbox)
- Groq API key (free)

### Steps
```bash
# 1. Clone the repo
git clone https://github.com/AMITYADAV-16/Nivesh-Mitra.git
cd Nivesh-Mitra

# 2. Create environment file
cp .env.example .env
# Fill in your keys in .env

# 3. Start all 8 services
docker compose up --build

# 4. Verify all services are running
docker compose ps
```

### Set Twilio Webhook
After starting, set your Twilio sandbox webhook to:
http://localhost:8082/whatsapp/webhook
Or with ngrok:
https://your-domain.ngrok-free.app/whatsapp/webhook

---

## 📱 Sample Conversation
User:    Hi
Bot:     Hi 👋 Main aapki financial details ke basis par
ek simple investment plan banaunga...
Aapka naam kya hai?
User:    Rahul
Bot:     Theek hai, Rahul. Aapki age kya hai?
User:    25
Bot:     Aapki monthly income kitni hai?
...after 5 questions + 5 bias questions...
Bot:     ━━━━━━━━━━━━━━━━━
📊 Rahul ka Investment Plan
━━━━━━━━━━━━━━━━━
🎯 Goal: Wealth Creation
💰 Target: Rs 35 Lakh in 5 years
📈 Required SIP: Rs 42,856/month
...

---

## 📂 Project Structure
Nivesh-Mitra/
├── api-gateway/
├── eureka-server/
├── user-profile-service/
├── risk-profiling-service/
├── market-data-service/
├── behavioral-bias-service/
├── recommendation-service/
├── whatsapp-service/
├── docker-compose.yml
├── .env.example
└── README.md

---

## 🧠 How Behavioral Bias Detection Works

The bot asks 5 questions about investment behavior:

| Question | Bias Detected |
|---|---|
| What do you do when fund drops 20%? | Loss Aversion |
| Friend says everyone is buying crypto? | Herd Mentality |
| Market crashed last month, next month? | Recency Bias |
| How do you invest Rs 50,000? | Overconfidence |
| How often do you check portfolio? | Status Quo Bias |

The detected bias directly influences the AI recommendation —
e.g. loss aversion → SIP recommended over lump sum.

---

## 👨‍💻 Author

**Amit Yadav**
- GitHub: [@AMITYADAV-16](https://github.com/AMITYADAV-16)
- Email: amityadav32279@gmail.com

---

## ⚠️ Disclaimer

This bot provides educational information only, not financial advice.
Always consult a SEBI-registered advisor before investing.