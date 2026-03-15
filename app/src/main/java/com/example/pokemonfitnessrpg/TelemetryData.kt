package com.example.pokemonfitnessrpg

import com.google.gson.annotations.SerializedName

data class TelemetryData(
    @SerializedName("heart_rate") val heartRate: Int,
    @SerializedName("steps") val steps: Int,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("health_streak") val healthStreak: Int,
    @SerializedName("action") val action: String
)

// NEW: The massive data payload from the AI
data class CaughtEntityDto(
    @SerializedName("name") val name: String,
    @SerializedName("type_1") val type1: String,
    @SerializedName("type_2") val type2: String,
    @SerializedName("rarity") val rarity: String,
    @SerializedName("hp") val hp: Int,
    @SerializedName("attack") val attack: Int,
    @SerializedName("defense") val defense: Int,
    @SerializedName("height") val height: String,
    @SerializedName("weight") val weight: String,
    @SerializedName("fast_move") val fastMove: String,
    @SerializedName("charged_move") val chargedMove: String,
    @SerializedName("nature") val nature: String,
    @SerializedName("description") val description: String,
    @SerializedName("evolution_stage") val evolutionStage: String
)

data class EncounterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("event") val event: String,
    @SerializedName("perk_unlocked") val perkUnlocked: String,
    @SerializedName("quiz_active") val quizActive: Boolean,
    @SerializedName("caught_entity") val caughtEntity: CaughtEntityDto? // Nullable if no catch happened
)

data class QuizRequest(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class QuizResponse(
    @SerializedName("status") val status: String,
    @SerializedName("question") val question: String,
    @SerializedName("correct_answer") val correctAnswer: Boolean
)