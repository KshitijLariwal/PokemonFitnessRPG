# 🌌 Cyber-Physical Fitness RPG Backend (`pokemon-go-wear-backend`)

A cloud-native, location-aware generative AI backend designed to power a Wear OS fitness RPG. This service bridges real-time human biometric telemetry (heart rate, steps) with a Retrieval-Augmented Generation (RAG) pipeline to dynamically generate location-based gameplay, entities, and trivia.

## 🚀 System Architecture & Tech Stack

This backend acts as the Game Master, ingesting physical state data from an edge device (smartwatch) and processing it through Google Cloud's AI and data infrastructure.

* **Framework:** FastAPI (Python 3.12+)
* **LLM Orchestration:** LangChain
* **Generative AI:** Google Gemini 2.5 Flash 
* **RAG / Vector Search:** Google Cloud Vertex AI (Discovery Engine)
* **Data Warehousing:** Google Cloud BigQuery
* **Geolocation:** Geopy (Nominatim)

## ✨ Core Features

* **Biometric State Engine:** Reads real-time heart rate and step-streak telemetry to determine the player's physical state (e.g., resting vs. sprinting), adjusting game difficulty, entity rarity, and Combat Power (CP) math accordingly.
* **Location-Aware RAG:** Uses reverse-geocoding to identify the player's real-world city, querying a Vertex AI vector database to retrieve local lore and history.
* **Dynamic Entity Generation:** Instructs Gemini to invent completely unique, structurally sound JSON entities (types, stats, movesets, flavor text) inspired by the player's physical environment.
* **On-the-Fly Trivia:** Generates geographically localized True/False fitness and lore quizzes to test the player during gameplay.
* **Batch Telemetry Logging:** Streams all player encounters, physical stats, and GPS coordinates into BigQuery for analytics and future ML training.

## 📡 API Reference

### 1. Process Telemetry & Generate Encounter
`POST /api/v1/telemetry`
Evaluates physical effort and location to generate a narrative event or a capture opportunity.

**Request Body:**
```json
{
  "heart_rate": 135,
  "steps": 3300,
  "lat": 28.6139,
  "lon": 77.2090,
  "health_streak": 6,
  "action": "sprint"
}

```

**Response:**

```json
{
  "status": "success",
  "event": "You sprint through the smog. An entity materializes!",
  "perk_unlocked": "",
  "quiz_active": true,
  "caught_entity": {
    "name": "Delhi-Zapdos",
    "type_1": "Electric",
    "type_2": "Smog",
    "rarity": "Epic",
    "hp": 120,
    "attack": 85,
    "defense": 60,
    "fast_move": "Static Shock",
    "charged_move": "Monsoon Strike",
    "description": "Born from the chaotic power grids and dense air of the capital.",
    "evolution_stage": "Basic"
  }
}

```

### 2. Request Dynamic Quiz

`POST /api/v1/quiz`
Generates a localized Aether trivia question based on the user's coordinates.

**Request Body:**

```json
{
  "lat": 28.6139,
  "lon": 77.2090
}

```

## 🛠️ Local Development Setup

1. **Clone the repository:**
```bash
git clone [https://github.com/yourusername/pokemon-go-wear-backend.git](https://github.com/yourusername/pokemon-go-wear-backend.git)
cd pokemon-go-wear-backend

```


2. **Install dependencies:**
```bash
pip install fastapi uvicorn langchain-google-genai langgraph google-cloud-bigquery google-cloud-discoveryengine geopy pydantic

```


3. **Configure Environment Variables:**
Ensure your Google Cloud credentials and Gemini API key are securely set in your environment.
```bash
export GOOGLE_API_KEY="your_gemini_api_key_here"
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-file.json"

```


4. **Run the FastAPI Server:**
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000

```


*The interactive API documentation will be available at `http://localhost:8000/docs`.*

## 🧠 Author

**Kshitij Lariwal** Specializing in Cyber-Physical Systems, Artificial Intelligence, and highly scalable full-stack architectures.

