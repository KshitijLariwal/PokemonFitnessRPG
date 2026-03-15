package com.example.pokemonfitnessrpg

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GameApiService {
    @POST("api/v1/telemetry")
    suspend fun sendTelemetry(@Body data: TelemetryData): Response<EncounterResponse>

    // NEW: The dynamic quiz endpoint
    @POST("api/v1/quiz")
    suspend fun getDynamicQuiz(@Body request: QuizRequest): Response<QuizResponse>
}