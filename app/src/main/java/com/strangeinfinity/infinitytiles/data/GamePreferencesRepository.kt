package com.strangeinfinity.infinitytiles.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.strangeinfinity.infinitytiles.domain.model.BoardSize
import com.strangeinfinity.infinitytiles.domain.model.GameMode
import com.strangeinfinity.infinitytiles.domain.model.GameSnapshot
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.GameStats
import com.strangeinfinity.infinitytiles.domain.model.Tile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "infinity_tiles_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class GamePreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_SCORE = longPreferencesKey("game_score")
        private val KEY_BEST_SCORE = longPreferencesKey("game_best_score")
        private val KEY_MOVES = intPreferencesKey("game_moves")
        private val KEY_TILES_DATA = stringPreferencesKey("game_tiles_data")
        private val KEY_IS_GAMEOVER = booleanPreferencesKey("game_is_gameover")
        private val KEY_IS_VICTORY = booleanPreferencesKey("game_is_victory")
        private val KEY_HAS_CONTINUED = booleanPreferencesKey("game_has_continued")
        private val KEY_BOARD_SIZE = intPreferencesKey("game_board_size")
        private val KEY_GAME_MODE = stringPreferencesKey("game_mode")
        private val KEY_PREV_SNAPSHOT = stringPreferencesKey("game_prev_snapshot")

        // Settings
        private val KEY_THEME = stringPreferencesKey("setting_theme")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("setting_sound")
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("setting_haptic")

        // Statistics
        private val KEY_STATS_PLAYED = longPreferencesKey("stats_played")
        private val KEY_STATS_WON = longPreferencesKey("stats_won")
        private val KEY_STATS_BEST_SCORE = longPreferencesKey("stats_best_score")
        private val KEY_STATS_HIGHEST_TILE = longPreferencesKey("stats_highest_tile")
        private val KEY_STATS_TOTAL_MERGES = longPreferencesKey("stats_total_merges")
        private val KEY_STATS_TOTAL_MOVES = longPreferencesKey("stats_total_moves")
    }

    // --- Settings Flows ---
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(raw)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    // --- Statistics Flow ---
    val gameStatsFlow: Flow<GameStats> = context.dataStore.data.map { prefs ->
        GameStats(
            gamesPlayed = prefs[KEY_STATS_PLAYED] ?: 0L,
            gamesWon = prefs[KEY_STATS_WON] ?: 0L,
            bestScore = prefs[KEY_STATS_BEST_SCORE] ?: 0L,
            highestTile = prefs[KEY_STATS_HIGHEST_TILE] ?: 0L,
            totalMerges = prefs[KEY_STATS_TOTAL_MERGES] ?: 0L,
            totalMoves = prefs[KEY_STATS_TOTAL_MOVES] ?: 0L
        )
    }

    // --- Saved Game Flow ---
    val savedGameStateFlow: Flow<GameState?> = context.dataStore.data.map { prefs ->
        val tilesData = prefs[KEY_TILES_DATA]
        if (tilesData.isNullOrBlank()) {
            null
        } else {
            try {
                val tiles = deserializeTiles(tilesData)
                val score = prefs[KEY_SCORE] ?: 0L
                val bestScore = prefs[KEY_BEST_SCORE] ?: 0L
                val moves = prefs[KEY_MOVES] ?: 0
                val isGameOver = prefs[KEY_IS_GAMEOVER] ?: false
                val isVictory = prefs[KEY_IS_VICTORY] ?: false
                val hasContinued = prefs[KEY_HAS_CONTINUED] ?: false
                val boardSizeDim = prefs[KEY_BOARD_SIZE] ?: 4
                val modeStr = prefs[KEY_GAME_MODE] ?: GameMode.CLASSIC.name
                val prevSnapshotData = prefs[KEY_PREV_SNAPSHOT]
                val prevSnapshot = prevSnapshotData?.let { deserializeSnapshot(it) }

                GameState(
                    boardSize = BoardSize.fromDimension(boardSizeDim),
                    gameMode = GameMode.fromName(modeStr),
                    tiles = tiles,
                    score = score,
                    bestScore = bestScore,
                    movesCount = moves,
                    isGameOver = isGameOver,
                    isVictory = isVictory,
                    hasContinuedAfterVictory = hasContinued,
                    previousSnapshot = prevSnapshot
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun saveGameState(state: GameState) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TILES_DATA] = serializeTiles(state.tiles)
            prefs[KEY_SCORE] = state.score
            prefs[KEY_BEST_SCORE] = state.bestScore
            prefs[KEY_MOVES] = state.movesCount
            prefs[KEY_IS_GAMEOVER] = state.isGameOver
            prefs[KEY_IS_VICTORY] = state.isVictory
            prefs[KEY_HAS_CONTINUED] = state.hasContinuedAfterVictory
            prefs[KEY_BOARD_SIZE] = state.boardSize.dimension
            prefs[KEY_GAME_MODE] = state.gameMode.name

            val prev = state.previousSnapshot
            if (prev != null) {
                prefs[KEY_PREV_SNAPSHOT] = serializeSnapshot(prev)
            } else {
                prefs.remove(KEY_PREV_SNAPSHOT)
            }
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = themeMode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun recordGameEnded(won: Boolean, finalScore: Long, highestTile: Long, merges: Long, moves: Long) {
        context.dataStore.edit { prefs ->
            val played = (prefs[KEY_STATS_PLAYED] ?: 0L) + 1L
            val currentWon = prefs[KEY_STATS_WON] ?: 0L
            val newWon = if (won) currentWon + 1L else currentWon
            val best = maxOf(prefs[KEY_STATS_BEST_SCORE] ?: 0L, finalScore)
            val highest = maxOf(prefs[KEY_STATS_HIGHEST_TILE] ?: 0L, highestTile)
            val totalMerges = (prefs[KEY_STATS_TOTAL_MERGES] ?: 0L) + merges
            val totalMoves = (prefs[KEY_STATS_TOTAL_MOVES] ?: 0L) + moves

            prefs[KEY_STATS_PLAYED] = played
            prefs[KEY_STATS_WON] = newWon
            prefs[KEY_STATS_BEST_SCORE] = best
            prefs[KEY_STATS_HIGHEST_TILE] = highest
            prefs[KEY_STATS_TOTAL_MERGES] = totalMerges
            prefs[KEY_STATS_TOTAL_MOVES] = totalMoves
        }
    }

    suspend fun resetStatistics() {
        context.dataStore.edit { prefs ->
            prefs[KEY_STATS_PLAYED] = 0L
            prefs[KEY_STATS_WON] = 0L
            prefs[KEY_STATS_BEST_SCORE] = 0L
            prefs[KEY_STATS_HIGHEST_TILE] = 0L
            prefs[KEY_STATS_TOTAL_MERGES] = 0L
            prefs[KEY_STATS_TOTAL_MOVES] = 0L
        }
    }

    // --- Helpers for fast compact serialization ---
    // Format: "id:value:row:col;id:value:row:col"
    private fun serializeTiles(tiles: List<Tile>): String {
        return tiles.joinToString(";") { "${it.id}:${it.value}:${it.row}:${it.col}" }
    }

    private fun deserializeTiles(data: String): List<Tile> {
        if (data.isBlank()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 4) {
                val id = parts[0].toLongOrNull() ?: return@mapNotNull null
                val value = parts[1].toLongOrNull() ?: return@mapNotNull null
                val row = parts[2].toIntOrNull() ?: return@mapNotNull null
                val col = parts[3].toIntOrNull() ?: return@mapNotNull null
                Tile(id = id, value = value, row = row, col = col)
            } else null
        }
    }

    // Format: "score|moves|isGameOver|isVictory|hasContinued|tilesData"
    private fun serializeSnapshot(snapshot: GameSnapshot): String {
        val tilesStr = serializeTiles(snapshot.tiles)
        return "${snapshot.score}|${snapshot.movesCount}|${snapshot.isGameOver}|${snapshot.isVictory}|${snapshot.hasContinuedAfterVictory}|$tilesStr"
    }

    private fun deserializeSnapshot(data: String): GameSnapshot? {
        val parts = data.split("|")
        if (parts.size < 6) return null
        val score = parts[0].toLongOrNull() ?: 0L
        val moves = parts[1].toIntOrNull() ?: 0
        val isGameOver = parts[2].toBooleanStrictOrNull() ?: false
        val isVictory = parts[3].toBooleanStrictOrNull() ?: false
        val hasContinued = parts[4].toBooleanStrictOrNull() ?: false
        val tiles = deserializeTiles(parts[5])
        return GameSnapshot(
            tiles = tiles,
            score = score,
            movesCount = moves,
            isGameOver = isGameOver,
            isVictory = isVictory,
            hasContinuedAfterVictory = hasContinued
        )
    }
}
