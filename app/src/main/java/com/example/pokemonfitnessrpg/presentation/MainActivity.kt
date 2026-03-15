package com.example.pokemonfitnessrpg.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.example.pokemonfitnessrpg.*
import com.example.pokemonfitnessrpg.presentation.theme.PokemonFitnessRPGTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- THE IMMERSIVE BACKGROUND GRADIENT ---
val AetherGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0B0F19), // Midnight Dark
        Color(0xFF1A1A2E), // Deep Cyber Purple
        Color(0xFF0F3460)  // Aether Blue
    )
)

// --- VIBRANT CYBERPUNK BUTTON COLORS ---
val NeonCyan = Color(0xFF00E5FF)
val NeonRed = Color(0xFFFF2A55)
val SynthPurple = Color(0xFF8A2BE2)
val NeonGreen = Color(0xFF00E676)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            PokemonFitnessRPGTheme {
                AppScaffold {
                    GameScreen(database)
                }
            }
        }
    }
}

@Composable
fun GameScreen(database: AppDatabase) {
    var currentScreen by remember { mutableStateOf("explore") }
    var selectedPokemon by remember { mutableStateOf<PokemonEntity?>(null) }

    var encounterText by remember {
        mutableStateOf("The digital Aether hums around you. The neighborhood is quiet... for now. What is your move?")
    }
    var activePerk by remember { mutableStateOf("") }

    var dynamicQuestion by remember { mutableStateOf("Loading Aether Trivia...") }
    var dynamicAnswer by remember { mutableStateOf(true) }

    val onQuizFinished = { won: Boolean ->
        currentScreen = "explore"
        encounterText = if (won) {
            "Quiz Passed! Your knowledge empowers your scanner. You gained 100 Bonus CP for your next encounter!"
        } else {
            "Quiz Failed. The Aether energy fades. Keep walking and brush up on your health facts!"
        }
    }

    when (currentScreen) {
        "quiz" -> QuizScreen(
            question = dynamicQuestion,
            correctAnswer = dynamicAnswer,
            onQuizFinished = onQuizFinished
        )
        "pokedex" -> PokedexScreen(
            database = database,
            onBack = { currentScreen = "explore" },
            onPokemonClick = { pokemon ->
                selectedPokemon = pokemon
                currentScreen = "details"
            }
        )
        "details" -> selectedPokemon?.let {
            PokemonDetailScreen(pokemon = it, onBack = { currentScreen = "pokedex" })
        }
        else -> MainExplorationScreen(
            database = database,
            encounterText = encounterText,
            activePerk = activePerk,
            onUpdateEncounter = { text, perk ->
                encounterText = text
                if (perk.isNotEmpty()) activePerk = perk
            },
            onQuizTriggered = { question, answer ->
                dynamicQuestion = question
                dynamicAnswer = answer
                currentScreen = "quiz"
            },
            onOpenPokedex = { currentScreen = "pokedex" }
        )
    }
}

@Composable
fun MainExplorationScreen(
    database: AppDatabase,
    encounterText: String,
    activePerk: String,
    onUpdateEncounter: (String, String) -> Unit,
    onQuizTriggered: (String, Boolean) -> Unit,
    onOpenPokedex: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var displayedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var skipTyping by remember { mutableStateOf(false) }

    var simulatedHeartRate by remember { mutableIntStateOf(85) }
    var simulatedSteps by remember { mutableIntStateOf(2500) }
    val calculatedHealthStreak = (simulatedSteps / 500).coerceIn(1, 10)

    val testLat = 28.6139
    val testLon = 77.2090

    val listState = rememberTransformingLazyColumnState()

    LaunchedEffect(encounterText) {
        isTyping = true
        skipTyping = false
        displayedText = ""

        for (i in encounterText.indices) {
            if (skipTyping) {
                displayedText = encounterText
                break
            }
            displayedText += encounterText[i]
            delay(35)
        }
        isTyping = false
    }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(AetherGradient)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "❤️ $simulatedHeartRate  |  👟 $simulatedSteps",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    if (activePerk.isNotEmpty()) {
                        Text(
                            text = "🌟 $activePerk",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = displayedText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        .clickable { if (isTyping) skipTyping = true }
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp).size(24.dp),
                        strokeWidth = 2.dp,
                        colors = ProgressIndicatorDefaults.colors(
                            indicatorColor = NeonCyan
                        )
                    )
                }
            }

            if (!isTyping && !isLoading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                simulatedHeartRate = 85
                                simulatedSteps += 200

                                coroutineScope.launch {
                                    try {
                                        val telemetry = TelemetryData(
                                            heartRate = simulatedHeartRate, steps = simulatedSteps,
                                            lat = testLat, lon = testLon, healthStreak = calculatedHealthStreak, action = "walk"
                                        )
                                        val response = RetrofitClient.apiService.sendTelemetry(telemetry)
                                        if (response.isSuccessful && response.body() != null) {
                                            val data = response.body()!!
                                            onUpdateEncounter(data.event, data.perkUnlocked)

                                            if (data.quizActive) {
                                                val quizRes = RetrofitClient.apiService.getDynamicQuiz(QuizRequest(testLat, testLon))
                                                if (quizRes.isSuccessful && quizRes.body() != null) {
                                                    onQuizTriggered(quizRes.body()!!.question, quizRes.body()!!.correctAnswer)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) { onUpdateEncounter("Comms offline.", "") }
                                    finally { isLoading = false }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Scout",
                                color = Color.Black,
                                fontSize = 12.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                simulatedHeartRate = 135
                                simulatedSteps += 800

                                coroutineScope.launch {
                                    try {
                                        val telemetry = TelemetryData(
                                            heartRate = simulatedHeartRate, steps = simulatedSteps,
                                            lat = testLat, lon = testLon, healthStreak = calculatedHealthStreak, action = "sprint"
                                        )
                                        val response = RetrofitClient.apiService.sendTelemetry(telemetry)
                                        if (response.isSuccessful && response.body() != null) {
                                            val data = response.body()!!
                                            onUpdateEncounter(data.event, data.perkUnlocked)

                                            if (calculatedHealthStreak >= 5 && data.caughtEntity != null) {
                                                val ai = data.caughtEntity
                                                val caught = PokemonEntity(
                                                    dexNumber = (1..999).random(),
                                                    name = ai.name,
                                                    type1 = ai.type1,
                                                    type2 = ai.type2,
                                                    rarity = ai.rarity,
                                                    cp = (calculatedHealthStreak * 100) + ai.attack + ai.defense,
                                                    hp = ai.hp,
                                                    baseAttack = ai.attack,
                                                    baseDefense = ai.defense,
                                                    fastMove = ai.fastMove,
                                                    chargedMove = ai.chargedMove,
                                                    nature = ai.nature,
                                                    height = ai.height,
                                                    weight = ai.weight,
                                                    flavorText = ai.description,
                                                    evolutionStage = ai.evolutionStage,
                                                    aetherDust = (100..500).random(),
                                                    captureLocation = "New Delhi",
                                                    wardenHeartRate = simulatedHeartRate,
                                                    wardenHealthStreak = calculatedHealthStreak
                                                )
                                                database.pokemonDao().insertPokemon(caught)
                                            }

                                            if (data.quizActive) {
                                                val quizRes = RetrofitClient.apiService.getDynamicQuiz(QuizRequest(testLat, testLon))
                                                if (quizRes.isSuccessful && quizRes.body() != null) {
                                                    onQuizTriggered(quizRes.body()!!.question, quizRes.body()!!.correctAnswer)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) { onUpdateEncounter("Comms offline.", "") }
                                    finally { isLoading = false }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonRed,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Engage",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = onOpenPokedex,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SynthPurple,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "View Pokédex",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizScreen(question: String, correctAnswer: Boolean, onQuizFinished: (Boolean) -> Unit) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding, state = listState,
            modifier = Modifier.fillMaxSize().background(AetherGradient)
        ) {
            item {
                Text(
                    text = "Aether Trivia!",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            item {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = { onQuizFinished(correctAnswer) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "True",
                            color = Color.Black,
                            fontSize = 12.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = { onQuizFinished(!correctAnswer) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "False",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PokedexScreen(database: AppDatabase, onBack: () -> Unit, onPokemonClick: (PokemonEntity) -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    val inventory by database.pokemonDao().getAllPokemon().collectAsState(initial = emptyList())

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding, state = listState,
            modifier = Modifier.fillMaxSize().background(AetherGradient)
        ) {
            item {
                Text(
                    text = "AetherDex Archive",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            if (inventory.isEmpty()) {
                item {
                    Text(
                        text = "No entities captured. Engage anomalies to archive them!",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                items(count = inventory.size) { index ->
                    val pokemon = inventory[index]
                    val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(pokemon.captureDate))

                    Card(
                        onClick = { onPokemonClick(pokemon) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = pokemon.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = "CP: ${pokemon.cp} | Caught: $dateStr", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SynthPurple, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Back to Map",
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonDetailScreen(pokemon: PokemonEntity, onBack: () -> Unit) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding, state = listState,
            modifier = Modifier.fillMaxSize().background(AetherGradient)
        ) {
            item {
                Text(
                    text = "#${pokemon.dexNumber} ${pokemon.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 24.dp, bottom = 4.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = "${pokemon.type1} / ${pokemon.type2} | ${pokemon.rarity}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonRed,
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = pokemon.flavorText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Combat Power: ${pokemon.cp}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("HP: ${pokemon.hp} | ATK: ${pokemon.baseAttack} | DEF: ${pokemon.baseDefense}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text("Fast: ${pokemon.fastMove} | Charged: ${pokemon.chargedMove}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("--- Warden Bio-Data ---", color = SynthPurple, style = MaterialTheme.typography.labelMedium)
                    Text("Location: ${pokemon.captureLocation}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text("Heart Rate at Capture: ${pokemon.wardenHeartRate} BPM", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text("Consistency Score: ${pokemon.wardenHealthStreak}/10", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }

            item {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SynthPurple, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Close Archive",
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}