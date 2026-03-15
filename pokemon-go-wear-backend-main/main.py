import os
import json
import tempfile
import asyncio
from datetime import datetime
from fastapi import FastAPI
from pydantic import BaseModel
from typing import TypedDict
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import StateGraph, START, END
from google.cloud import bigquery
from google.cloud import discoveryengine_v1 as discoveryengine
from geopy.geocoders import Nominatim

app = FastAPI()

# --- Configuration ---
PROJECT_ID = "pokemon-490205" 
TABLE_ID = f"{PROJECT_ID}.pokemon_rpg_data.player_encounters"
DATA_STORE_ID = "pokemon-lore-db_1773475919521"
LOCATION = "global"

bq_client = bigquery.Client(project=PROJECT_ID)
geolocator = Nominatim(user_agent="pokemon_rpg_backend")

# 1. UPDATED SCHEMAS: Added health_streak, action, perks, and quizzes
class TelemetryData(BaseModel):
    heart_rate: int
    steps: int
    lat: float
    lon: float
    health_streak: int
    action: str

# NEW: Schema for requesting a dynamic quiz
class QuizRequest(BaseModel):
    lat: float
    lon: float

class GameState(TypedDict):
    heart_rate: int
    steps: int
    lat: float
    lon: float
    city: str
    health_streak: int
    action: str
    encounter: str
    perk_unlocked: str
    quiz_active: bool

# Initialize Gemini 
llm = ChatGoogleGenerativeAI(
    model="gemini-2.5-flash",
    google_api_key="" # Put your fresh API key here!
)

# ... (Keep get_city_from_coords and search_game_lore functions exactly the same) ...
async def get_city_from_coords(lat: float, lon: float) -> str:
    try:
        location = await asyncio.to_thread(geolocator.reverse, f"{lat}, {lon}", exactly_one=True)
        if not location:
            return "the wilderness"
        address = location.raw.get('address', {}) # type: ignore
        return address.get('city', address.get('town', 'the wilderness'))
    except Exception as e:
        return "the wilderness"

def search_game_lore(city_name: str) -> str:
    client = discoveryengine.SearchServiceClient()
    serving_config = client.serving_config_path(
        project=PROJECT_ID, location=LOCATION, data_store=DATA_STORE_ID, serving_config="default_config"
    )
    request = discoveryengine.SearchRequest(
        serving_config=serving_config, query=f"lore about {city_name}", page_size=1
    )
    try:
        response = client.search(request)
        for result in response.results:
            return result.document.derived_struct_data["snippets"][0]["snippet"]
    except Exception as e:
        print(f"Vertex Search Error: {e}")
    return "No specific lore found for this region."


# 4. THE POKÉMON GAME MASTER NODE
def generate_encounter(state: GameState):
    hr = state["heart_rate"]
    streak = state["health_streak"]
    action = state["action"]
    city = state["city"]
    
    retrieved_lore = search_game_lore(city)
    
    prompt = f"""
    You are a Game Master for a Cyber-Physical Fitness RPG. The player is in {city}.
    
    PLAYER STATS:
    - Action: {action}
    - Heart Rate: {hr} bpm
    - Health Streak: {streak}/10
    
    LOCAL LORE: "{retrieved_lore}"
    
    RULES:
    1. If action is 'walk': Generate a peaceful discovery (event text, maybe a perk, quiz). NO caught entity.
    2. If action is 'sprint' AND Health Streak is 5 or higher: The player catches an Aether Entity!
       You MUST include a "caught_entity" JSON object. The entity's design, types, and lore description MUST be inspired by {city}.
       
    CONSTRAINTS: Return strictly JSON. 
    Format example for successful catch:
    {{
        "event": "You sprint through the smog. An entity materializes!",
        "perk_unlocked": "Iron Lungs",
        "quiz_active": false,
        "caught_entity": {{
            "name": "Delhi-Zapdos",
            "type_1": "Electric",
            "type_2": "Smog",
            "rarity": "Epic",
            "hp": 120,
            "attack": 85,
            "defense": 60,
            "height": "1.5m",
            "weight": "45kg",
            "fast_move": "Static Shock",
            "charged_move": "Monsoon Strike",
            "nature": "Aggressive",
            "description": "Born from the chaotic power grids and dense air of the capital, this entity thrives in high-adrenaline environments.",
            "evolution_stage": "Basic"
        }}
    }}
    If no catch happens, omit the "caught_entity" key.
    """
    
    response = llm.invoke(prompt)
    
    try:
        content_str = str(response.content)
        if content_str.startswith("```json"):
            content_str = content_str.strip("`").removeprefix("json").strip()
            
        ai_data = json.loads(content_str)
        return {
            "encounter": ai_data.get("event", "You explore the area."),
            "perk_unlocked": ai_data.get("perk_unlocked", ""),
            "quiz_active": ai_data.get("quiz_active", False),
            "caught_entity": ai_data.get("caught_entity", None) # Pass the massive object if it exists!
        }
    except Exception as e:
        print(f"Failed to parse AI JSON: {e}")
        return {
            "encounter": "Scanner malfunction.",
            "perk_unlocked": "",
            "quiz_active": False,
            "caught_entity": None
        }
# 5. Build the Graph
workflow = StateGraph(GameState)
workflow.add_node("game_master", generate_encounter)
workflow.add_edge(START, "game_master")
workflow.add_edge("game_master", END)
game_app = workflow.compile()

# --- FastAPI Routes ---

@app.get("/")
async def root():
    return {"message": "Wear OS Game Backend is running with RAG!"}

@app.post("/api/v1/telemetry")
async def receive_telemetry(data: TelemetryData):
    city_name = await get_city_from_coords(data.lat, data.lon)

    initial_state: GameState = {
        "heart_rate": data.heart_rate,
        "steps": data.steps,
        "lat": data.lat,
        "lon": data.lon,
        "city": city_name,
        "health_streak": data.health_streak,
        "action": data.action,
        "encounter": "",
        "perk_unlocked": "",
        "quiz_active": False
    }
    
    result = game_app.invoke(initial_state)
    encounter_text = result['encounter']
    
    # Batch Load to BigQuery
    row_data = {
        "timestamp": datetime.utcnow().isoformat(),
        "heart_rate": data.heart_rate,
        "steps": data.steps,
        "latitude": data.lat,
        "longitude": data.lon,
        "encounter_text": encounter_text
    }
    
    with tempfile.NamedTemporaryFile(mode="w+", delete=False, suffix=".json") as temp_file:
        json.dump(row_data, temp_file)
        temp_file.write('\n')
        temp_file_path = temp_file.name

    try:
        job_config = bigquery.LoadJobConfig(
            source_format=bigquery.SourceFormat.NEWLINE_DELIMITED_JSON,
            autodetect=True, 
        )
        with open(temp_file_path, "rb") as source_file:
            job = bq_client.load_table_from_file(source_file, TABLE_ID, job_config=job_config)
        job.result() 
    except Exception as e:
        print(f"Failed to load data to BigQuery: {e}")
    finally:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)

    # Return everything to the Watch
    return {
        "status": "success", 
        "event": encounter_text,
        "perk_unlocked": result['perk_unlocked'],
        "quiz_active": result['quiz_active']
    }

@app.post("/api/v1/quiz")
async def generate_dynamic_quiz(data: QuizRequest):
    # 1. Reverse geocode to find where the player is
    city_name = await get_city_from_coords(data.lat, data.lon)
    
    # 2. Pull the local lore for that city
    retrieved_lore = search_game_lore(city_name)
    
    # 3. Instruct the Aether Quizmaster
    prompt = f"""
    You are the Aether Quizmaster testing a player in {city_name}.
    
    LOCAL LORE: "{retrieved_lore}"
    
    DIRECTIVE:
    Generate a True/False trivia question. It should either be about physical health/fitness, or creatively tie a health concept to the local lore of {city_name}.
    
    CONSTRAINTS:
    - Return your response in STRICT JSON format. Do not use markdown code blocks.
    - "question": The trivia question text (strictly under 20 words).
    - "correct_answer": A boolean (true or false).
    """
    
    # 4. Generate and Parse the JSON
    response = llm.invoke(prompt)
    
    try:
        content_str = str(response.content)
        if content_str.startswith("```json"):
            content_str = content_str.strip("`").removeprefix("json").strip()
            
        quiz_data = json.loads(content_str)
        return {
            "status": "success",
            "question": quiz_data.get("question", "Consistent daily exercise improves sleep quality."),
            "correct_answer": quiz_data.get("correct_answer", True)
        }
    except Exception as e:
        print(f"Failed to parse Quiz JSON: {e}")
        # Fallback question if the AI hallucinates bad JSON
        return {
            "status": "error",
            "question": "Hydration has no effect on your resting heart rate.",
            "correct_answer": False
        }