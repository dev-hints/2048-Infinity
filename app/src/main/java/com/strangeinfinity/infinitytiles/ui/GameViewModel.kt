package com.strangeinfinity.infinitytiles.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangeinfinity.infinitytiles.data.GamePreferencesRepository
import com.strangeinfinity.infinitytiles.data.ThemeMode
import com.strangeinfinity.infinitytiles.domain.engine.GameEngine
import com.strangeinfinity.infinitytiles.domain.model.BoardSize
import com.strangeinfinity.infinitytiles.domain.model.GameMode
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.GameStats
import com.strangeinfinity.infinitytiles.domain.model.MoveDirection
import com.strangeinfinity.infinitytiles.sound.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application)
    private val soundManager = SoundManager(application)
    private val engine = GameEngine()

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val soundEnabled: StateFlow<Boolean> = repository.soundEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val hapticEnabled: StateFlow<Boolean> = repository.hapticEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val gameStats: StateFlow<GameStats> = repository.gameStatsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameStats()
    )

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var initialLoaded = false

    init {
        viewModelScope.launch {
            val savedState = repository.savedGameStateFlow.first()
            if (savedState != null && savedState.tiles.isNotEmpty()) {
                _gameState.value = savedState
            } else {
                startNewGame(BoardSize.CLASSIC, GameMode.CLASSIC)
            }
            initialLoaded = true
        }
    }

    fun onMove(direction: MoveDirection) {
        val current = _gameState.value
        if (current.isGameOver) return

        val next = engine.move(current, direction)
        if (next == current) {
            // No movement took place
            return
        }

        val gained = next.score - current.score
        if (gained > 0) {
            soundManager.playMergeSound(soundEnabled.value)
            soundManager.triggerMergeHaptic(hapticEnabled.value)
        } else {
            soundManager.playMoveSound(soundEnabled.value)
            soundManager.triggerMoveHaptic(hapticEnabled.value)
        }

        if (next.isVictory && !current.isVictory) {
            soundManager.playVictorySound(soundEnabled.value)
            soundManager.triggerCelebrationHaptic(hapticEnabled.value)
            viewModelScope.launch {
                repository.recordGameEnded(
                    won = true,
                    finalScore = next.score,
                    highestTile = next.highestTile(),
                    merges = 1L,
                    moves = next.movesCount.toLong()
                )
            }
        }

        if (next.isGameOver && !current.isGameOver) {
            soundManager.playGameOverSound(soundEnabled.value)
            viewModelScope.launch {
                repository.recordGameEnded(
                    won = false,
                    finalScore = next.score,
                    highestTile = next.highestTile(),
                    merges = 0L,
                    moves = next.movesCount.toLong()
                )
            }
        }

        _gameState.value = next
        viewModelScope.launch {
            repository.saveGameState(next)
        }
    }

    fun onUndo() {
        val current = _gameState.value
        val restored = engine.undo(current)
        if (restored != current) {
            soundManager.triggerMoveHaptic(hapticEnabled.value)
            _gameState.value = restored
            viewModelScope.launch {
                repository.saveGameState(restored)
            }
        }
    }

    fun continueAfterVictory() {
        val updated = engine.continueAfterVictory(_gameState.value)
        _gameState.value = updated
        viewModelScope.launch {
            repository.saveGameState(updated)
        }
    }

    fun startNewGame(
        boardSize: BoardSize = _gameState.value.boardSize,
        gameMode: GameMode = _gameState.value.gameMode
    ) {
        val best = _gameState.value.bestScore

        if (gameMode == GameMode.DAILY_CHALLENGE) {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val seed = dateStr.toLongOrNull() ?: 20261022L
            engine.setRandom(Random(seed))
        } else {
            engine.setRandom(Random.Default)
        }

        val newState = engine.startNewGame(boardSize, gameMode, bestScore = best)
        _gameState.value = newState
        viewModelScope.launch {
            repository.saveGameState(newState)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticEnabled(enabled)
        }
    }

    fun changeBoardSize(boardSize: BoardSize) {
        startNewGame(boardSize = boardSize, gameMode = _gameState.value.gameMode)
    }

    fun changeGameMode(gameMode: GameMode) {
        startNewGame(boardSize = _gameState.value.boardSize, gameMode = gameMode)
    }

    fun resetStatistics() {
        viewModelScope.launch {
            repository.resetStatistics()
        }
    }

    /**
     * Debug helper to inject custom board state for deterministic test screenshot capture.
     */
    fun setDebugState(state: GameState) {
        _gameState.value = state
        viewModelScope.launch {
            repository.saveGameState(state)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
