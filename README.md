<h1 align="center">Nivesh Mitra</h1>
<h3 align="center">An AI investment advisor that lives on WhatsApp</h3>

<p align="center">
  Personalized SIP plans, behavioral bias detection, and multilingual advice — no app to download, no dashboard to learn.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Cloud-2025.0.1-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/LLaMA_3.3_70B-Groq-FF6B35?style=flat-square&logo=meta&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15-316192?style=flat-square&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/Twilio-WhatsApp-F22F46?style=flat-square&logo=twilio&logoColor=white"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square"/>
</p>

<p align="center">
  <a href="#why-i-built-this">Why</a> ·
  <a href="#what-it-does">What it does</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#demo">Demo</a> ·
  <a href="#running-it-locally">Running it locally</a> ·
  <a href="#api-docs">API docs</a> ·
  <a href="#whats-next">What's next</a>
</p>

---

## Why I built this

A lot of people around me family, friends, people I grew up with wants to invest but don't know hwere and which SIP to invest in but because every investment app feels like it was built for someone who already knows finance. Dashboards, jargon, risk profiles you have to fill out without knowing what half the terms mean.

Meanwhile, everyone already has WhatsApp open all day.

So the idea behind Nivesh Mitra was what if the advisor came to you, through a normal conversation, in whatever language you're comfortable in instead of you having to go figure out a new app? That's what this project is.

It also became a good excuse to actually build something with a proper microservices setup instead of just reading about it service discovery, an API gateway, circuit breakers, the whole thing. This README covers both sides what the product does, and how it's put together underneath.

---

## What it does

You chat with it like you'd chat with a friend who happens to know finance. It asks about 10 simple questions your income, expenses, what you're saving for, how many years you've got and then runs a short 5-question quiz that's actually a behavioral finance instrument in disguise. It's figuring out whether you're the type to panic sell when the market dips, or follow the crowd into whatever's trending, or hold on for way too long out of stubbornness.

Once it has all that, it hands everything off to an LLM (Groq's LLaMA 3.3 70B) which puts together an actual plan:

- A monthly SIP number, based on your real target and timeline
- A 10% yearly step-up so the plan grows with your income
- A reality check  if what you *should* invest is more than you can afford, it tells you and adjusts
- A 3-fund split matched to your risk profile
- A note on your investing personality, tailored to whichever bias the quiz picked up
- A basic emergency fund reminder before any of the SIP stuff even starts

All in Hindi, English, or Hinglish, whichever you pick at the start.

---
Here is the demo video link -> https://drive.google.com/file/d/1MCTmzFKOup26rAXCs5J9TtuTzC3TwtPJ/view?usp=drivesdk
## Architecture

It's 8 Spring Boot services behind a gateway, with Eureka handling discovery so nothing has hardcoded URLs to other services. Roughly:

```
WhatsApp User
     │
     ▼
 Twilio API  ──── Webhook (ngrok / cloud URL) ────►
     │
     ▼
┌─────────────────────────────────────┐
│         API Gateway  :8082          │   single entry point for everything
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│       WhatsApp Service  :8087       │   holds the conversation state,
│   Session mgmt · Twilio SDK         │   talks to Twilio directly
└────┬────────┬────────┬──────┬───────┘
     │        │        │      │
     ▼        ▼        ▼      ▼
  User     Risk    Market  Behavioral
 Profile  Profil-  Data    Bias
 :8081    ing :8083 :8084   :8086
   │                │
   ▼                ▼
PostgreSQL        Redis

     └──────────────────────────────►
              Recommendation
               Service :8085       ← pulls everything together, calls Groq
                  │
                  ▼
           Groq API (LLaMA 3.3 70B)

──────────────────────────────────────
 Eureka Server :8761  — everything above registers here
──────────────────────────────────────
```

**Why split it into 8 services instead of one app?** Mostly because I wanted to actually practice the patterns Feign clients for inter-service calls, Resilience4j circuit breakers on the Groq calls (the AI API is the one thing most likely to fail or time out), Eureka for discovery instead of hardcoded ports. It's more moving parts than a project like this strictly needs, but that was kind of the point.

**Worth knowing:** conversation state currently lives in memory in `whatsapp-service`, which means a restart wipes anyone's in-progress conversation. Moving that to Redis is the first thing on my list see [What's next](#whats-next).

---

## Stack

| Layer | Tech | Why |
|---|---|---|
| Language | Java 21 | Virtual threads, records, pattern matching — wanted to actually use the newer stuff, not just target it |
| Framework | Spring Boot 3.5 | |
| Cloud | Spring Cloud 2025.0.1 | Gateway, Eureka, Feign |
| AI | Groq + LLaMA 3.3 70B via Spring AI | Fast inference, generous free tier for a student project |
| Resilience | Resilience4j | Circuit breaker + retry, mainly guarding the Groq calls |
| DB | PostgreSQL 15 | User profiles |
| Cache | Redis 7 | Market data, so I'm not hammering external APIs |
| Messaging | Twilio WhatsApp API | |
| Containers | Docker Compose | One command, 8 services, no manual setup |

---

## Demo

Haven't deployed this anywhere public — running all 8 services means paying for hosting, which isn't really justified at this stage for a project I'm still actively rebuilding. So instead, a full run looks like this:

<details>
<summary>Click to expand — sample conversation</summary>

```
You:   Hi

Bot:   Hi 👋
       Main aapki financial details ke basis par
       ek simple investment plan banaunga.
       2–3 minute lagenge.
       Start karte hain — aapka naam kya hai?

You:   Rahul

Bot:   Theek hai, Rahul.
       Aapki age kya hai? (Sirf number, jaise 25)

You:   25

Bot:   Aapki monthly income kitni hai? (jaise 50000)

You:   60000

...  [income → expenses → dependents → goal → target → years → language]

Bot:   Almost done! 🎯
       Ab main aapki *investment personality* samjhunga.
       5 quick questions — har answer sirf A, B, C ya D mein.

       Q1: Aapne Rs 10,000 MF mein lagaye aur wo 20% gir gaya. Aap kya karenge?
       A) Turant bech do   B) Ruko   C) Aur kharido   D) Doston se pucho

You:   B

... [5 behavioral bias questions] ...

Bot:   ━━━━━━━━━━━━━━━━━
       📊 *Rahul ka Investment Plan*
       ━━━━━━━━━━━━━━━━━

       🎯 *Goal:* Wealth Creation
       💰 *Target:* Rs 35.0 Lakh in 5 years
       📈 *Required SIP:* Rs 42,856/month

       ━━━━━━━━━━━━━━━━━
       ⚠️ *Reality Check*
       Required SIP exceeds your savings.
       Smart move: Start with *Rs 15,000/month* 💪

       📅 *Step-Up Plan (10% yearly)*
       Year 1: *Rs 15,000* → Year 3: *Rs 18,150* → Year 5: *Rs 21,962*
       Projected corpus: *Rs 12.3 Lakh* at 12% CAGR

       ━━━━━━━━━━━━━━━━━
       📦 *Your 3-Fund Plan*

       1️⃣ *Mirae Asset Large Cap Fund*
          60% allocation · Rs 9,000/month
          _Stable large-cap for your moderate risk profile_

       2️⃣ *SBI Small Cap Fund*
          25% allocation · Rs 3,750/month
          _Growth kicker for long-term wealth creation_

       3️⃣ *Axis Liquid Fund*
          15% allocation · Rs 2,250/month
          _Liquidity buffer for short-term needs_

       ━━━━━━━━━━━━━━━━━
       🧠 *Your Investor Personality*
       Holding through a 20% drop shows strong conviction —
       don't let patience turn to inaction! ⚠️

       ━━━━━━━━━━━━━━━━━
       🛡️ *Safety First*
       Keep *Rs 1.8 Lakh* as emergency fund (6 months expenses).
       Invest only what's left after this.

       💡 _Hi bhejo naya plan banane ke liye_
          _Nivesh Mitra — aapka financial dost_ 💚
```

</details>

If you want to poke at it yourself, it runs locally with one Docker Compose command see [Running it locally](#running-it-locally).

---

## The bias quiz, if you're curious

Five questions, each one mapped to a classic behavioral finance bias:

| Question | What it's actually checking |
|---|---|
| Fund drops 20% — what do you do? | Loss aversion |
| Everyone's buying crypto — you? | Herd mentality |
| Market crashed last month — next move? | Recency bias |
| How do you invest Rs 50,000? | Overconfidence |
| How often do you check your portfolio? | Status quo bias |

Whatever bias comes out dominant gets fed straight into the prompt sent to Groq, so the plan's tone actually addresses it someone who's loss-averse gets nudged toward SIPs over lump-sum investing, someone prone to herd mentality gets a diversification reminder, and so on.

---

## Project layout

```
Nivesh-Mitra/
│
├── api-gateway/                  
├── eureka-server/                
├── user-profile-service/         # user financial profiles → Postgres
├── risk-profiling-service/       # conservative / moderate / aggressive banding
├── market-data-service/          # Indian MF data, cached in Redis
├── behavioral-bias-service/      # quiz answers → dominant bias
├── recommendation-service/       # builds the prompt, calls Groq, formats the plan
├── whatsapp-service/             # Twilio webhook + conversation state machine
│
├── docker-compose.yml
├── .env.example
├── .gitignore
├── LICENSE
└── README.md
```

---

## Running it locally

You'll need Java 21+, Docker Desktop, a free Twilio sandbox account, and a free Groq API key from [console.groq.com](https://console.groq.com). ngrok helps if you're testing the webhook locally.

```bash
git clone https://github.com/AMITYADAV-16/Nivesh-Mitra.git
cd Nivesh-Mitra
cp .env.example .env
```

Fill in `.env`:

```env
DB_PASSWORD=your_postgres_password
GROQ_API_KEY=gsk_xxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_WHATSAPP_NUMBER=+14155238886
```

Then:

```bash
docker compose up --build
```

This brings up Postgres, Redis, Eureka, and all 8 services. Check `docker compose ps` to make sure everything's healthy, and open [localhost:8761](http://localhost:8761) — you should see every service registered on the Eureka dashboard.

For the Twilio webhook, point ngrok at the gateway:

```bash
ngrok http 8082
```

Take the HTTPS URL ngrok gives you and drop it into the [Twilio Sandbox console](https://console.twilio.com/us1/develop/sms/try-it-out/whatsapp-learn) as:
```
https://your-domain.ngrok-free.app/whatsapp/webhook
```

Join the sandbox from your own WhatsApp, send `Hi`, and it should start the conversation.

---

## Ports, if you need them

| Service | Port | Notes |
|---|---|---|
| API Gateway | 8082 | the only one meant to be public |
| Eureka | 8761 | dashboard |
| WhatsApp Service | 8087 | receives Twilio webhooks |
| Recommendation Service | 8085 | where the AI call happens |
| User Profile | 8081 | internal |
| Risk Profiling | 8083 | internal |
| Market Data | 8084 | internal |
| Behavioral Bias | 8086 | internal |

---

## API docs

Each service exposes Swagger via springdoc-openapi, accessible at:

- `localhost:8081/swagger-ui.html` — user profiles
- `localhost:8083/swagger-ui.html` — risk profiling
- `localhost:8084/swagger-ui.html` — market data
- `localhost:8086/swagger-ui.html` — behavioral bias
- `localhost:8085/swagger-ui.html` — recommendations

---

## Patterns used

- **API Gateway** — one door in, instead of exposing 8 services directly
- **Service discovery via Eureka** — services find each other by name, not hardcoded IPs
- **Circuit breaker + retry (Resilience4j)** — the Groq API is the most likely thing to fail, so it's the most protected
- **Feign clients** — cleaner than writing raw RestTemplate calls everywhere
- **In-memory conversation state machine** — tracks where a user is mid-conversation (this is the piece that needs to move to Redis)
- **Prompt engineering with escape handling** — turns out AI-generated text with stray `%` characters breaks `String.format()` in interesting ways; that took a while to track down

---

## Security

All credentials go through `.env`, which is gitignored — nothing sensitive sits in the codebase. Services talk to each other over Docker's internal network, so only the gateway, Eureka, recommendation service, and WhatsApp service are reachable from outside. Financial data collected during a conversation stays in the local Postgres instance; the only external call involving user data is the one prompt sent to Groq to generate the plan.

---

## What's next

- [ ] Move session state into Redis so a restart doesn't lose active conversations
- [ ] Actual test coverage — JUnit + Mockito
- [ ] Monthly SIP check-in messages via scheduled Twilio jobs
- [ ] Stock portfolio analyzer using NSE/BSE data
- [ ] Maybe a Telegram version alongside WhatsApp
- [ ] Deploy properly once the project stabilizes

---

## Disclaimer

This isn't financial advice it's an educational project. Please don't make real investment decisions off a chatbot output without talking to a SEBI-registered advisor first. Mutual funds are subject to market risk. Read the scheme documents. All the usual disclaimers apply — and they apply for real here too.

---

## License

MIT — see [LICENSE](LICENSE).

---

## About me

**Amit Yadav** — B-Tech student, building in public.

- GitHub: [@AMITYADAV-16](https://github.com/AMITYADAV-16)
- LinkedIn: [Amit Yadav](https://www.linkedin.com/in/amit-yadav-0b1126300/)
- Email: [amityadav32279@gmail.com](mailto:amityadav32279@gmail.com)
