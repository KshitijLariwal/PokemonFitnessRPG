package com.example.pokemonfitnessrpg

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pokemon_inventory")
data class PokemonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dexNumber: Int,
    val name: String,
    val type1: String,
    val type2: String,
    val rarity: String,

    // Combat Stats
    val cp: Int,
    val hp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val fastMove: String,
    val chargedMove: String,
    val nature: String,

    // Bio & Lore
    val height: String,
    val weight: String,
    val flavorText: String,
    val evolutionStage: String,
    val aetherDust: Int,

    // THE CYBER-PHYSICAL CONTEXT (Your secret weapon!)
    val captureLocation: String,
    val captureDate: Long = System.currentTimeMillis(),
    val wardenHeartRate: Int,
    val wardenHealthStreak: Int
)

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    @Query("SELECT * FROM pokemon_inventory ORDER BY captureDate DESC")
    fun getAllPokemon(): Flow<List<PokemonEntity>>
}

@Database(entities = [PokemonEntity::class], version = 2, exportSchema = false) // Bumped version to 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // FallbackToDestructiveMigration allows us to wipe the old small table and create this massive new one
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_rpg_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}