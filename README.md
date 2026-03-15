# ⌚ Cyber-Physical Fitness RPG - Wear OS Client (`pokemon-go-wear-client`)

An immersive, location-aware fitness RPG built natively for Wear OS. This application transforms real-world biological telemetry (heart rate, step counts, and GPS coordinates) into a cyberpunk-themed interactive adventure, connecting physical kinetic effort to a cloud-based Generative AI engine.

## 📱 Experience & UI

The app abandons traditional fitness dashboards for a narrative-driven, interactive audio-book experience on the wrist.
* **Responsive Neon Interface:** Built entirely in Jetpack Compose for Wear OS, featuring immersive gradients, custom auto-scaling typography to prevent screen overflow, and dynamic typewriter narrative animations.
* **The AetherDex Archive:** A fully functional on-device Pokédex that stores AI-generated entities, complete with custom lore and the exact biological metrics (BPM, health streak) the user achieved at the moment of capture.

## 🛠️ Tech Stack & Architecture

This project demonstrates edge-device data persistence, asynchronous state management, and seamless cloud synchronization.

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose for Wear OS (`TransformingLazyColumn`, custom state management)
* **Local Persistence:** Room Database (SQLite) with Coroutines and Kotlin `Flow` for reactive UI updates
* **Networking:** Retrofit2 & Gson (RESTful communication with the Python/FastAPI backend)
* **Asynchrony:** Kotlin Coroutines (`viewModelScope`, `LaunchedEffect`)
* **Hardware Integrations:** Google Play Services for Location, Wear OS Health Services for biometrics

## ✨ Core Mechanics

* **Scout (Low Intensity):** Triggers a "Walk" action. The watch sends baseline telemetry to the backend, returning localized narrative events, health perks, or dynamic True/False trivia based on the real-world city.
* **Engage (High Intensity):** Triggers a "Sprint" action. Spikes in heart rate and consistent health streaks instruct the cloud AI to generate a geographically unique entity. 
* **Edge Storage:** Captures are permanently saved to the watch's local Room database, merging the AI's generated stats (Types, Movesets, Lore) with the user's real-world physical data (Location, Capture BPM).

## 🚀 Local Development Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/pokemon-go-wear-client.git
   ```

2. **Open in Android Studio:**
   Ensure you are using Android Studio Ladybug (or newer) with the Wear OS emulator installed (API 30+ recommended).
3. **Configure the Backend Connection:**
   Open `RetrofitClient.kt` and update the `BASE_URL` to point to your active FastAPI backend instance.
```kotlin
// Example for GitHub Codespaces or local network testing
private const val BASE_URL = "https://your-backend-url.app.github.dev/"
```


4. **Build and Run:**
   Sync the Gradle project (ensure `ksp` and modern Version Catalogs are correctly resolving) and deploy to your Wear OS emulator or physical smartwatch.

## 🧠 Author

**Kshitij Lariwal** – Specializing in Cyber-Physical Systems, Artificial Intelligence, and highly scalable full-stack architectures.
