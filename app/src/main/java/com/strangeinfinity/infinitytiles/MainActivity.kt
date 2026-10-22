package com.strangeinfinity.infinitytiles

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.strangeinfinity.infinitytiles.data.ThemeMode
import com.strangeinfinity.infinitytiles.domain.model.BoardSize
import com.strangeinfinity.infinitytiles.domain.model.GameMode
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.MoveDirection
import com.strangeinfinity.infinitytiles.domain.model.Tile
import com.strangeinfinity.infinitytiles.ui.GameViewModel
import com.strangeinfinity.infinitytiles.ui.screens.GameScreen
import com.strangeinfinity.infinitytiles.ui.theme.InfinityTilesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private var showSettingsState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleTestIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            InfinityTilesTheme(themeMode = themeMode) {
                GameScreen(
                    viewModel = viewModel,
                    initialShowSettings = showSettingsState
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTestIntent(intent)
    }

    private fun handleTestIntent(intent: Intent?) {
        val action = intent?.getStringExtra("test_action") ?: return
        when (action) {
            "SET_GAMEPLAY" -> {
                showSettingsState = false
                val tiles = listOf(
                    Tile(1, 1024, 0, 0), Tile(2, 512, 0, 1), Tile(3, 256, 0, 2), Tile(4, 128, 0, 3),
                    Tile(5, 64, 1, 0), Tile(6, 32, 1, 1), Tile(7, 16, 1, 2), Tile(8, 8, 1, 3),
                    Tile(9, 4, 2, 0), Tile(10, 4, 2, 1), Tile(11, 2, 2, 2)
                )
                viewModel.setDebugState(
                    GameState(
                        boardSize = BoardSize.CLASSIC,
                        gameMode = GameMode.CLASSIC,
                        tiles = tiles,
                        score = 9480L,
                        bestScore = 14200L,
                        movesCount = 142,
                        isGameOver = false,
                        isVictory = false
                    )
                )
            }
            "SET_VICTORY" -> {
                showSettingsState = false
                val tiles = listOf(
                    Tile(1, 2048, 0, 0), Tile(2, 512, 0, 1), Tile(3, 256, 0, 2), Tile(4, 128, 0, 3),
                    Tile(5, 64, 1, 0), Tile(6, 32, 1, 1), Tile(7, 16, 1, 2), Tile(8, 8, 1, 3),
                    Tile(9, 4, 2, 0), Tile(10, 2, 2, 1)
                )
                viewModel.setDebugState(
                    GameState(
                        boardSize = BoardSize.CLASSIC,
                        gameMode = GameMode.CLASSIC,
                        tiles = tiles,
                        score = 24360L,
                        bestScore = 24360L,
                        movesCount = 388,
                        isGameOver = false,
                        isVictory = true,
                        hasContinuedAfterVictory = false
                    )
                )
            }
            "SET_GAME_OVER" -> {
                showSettingsState = false
                val fullTiles = listOf(
                    Tile(1, 2, 0, 0), Tile(2, 4, 0, 1), Tile(3, 2, 0, 2), Tile(4, 4, 0, 3),
                    Tile(5, 4, 1, 0), Tile(6, 2, 1, 1), Tile(7, 4, 1, 2), Tile(8, 2, 1, 3),
                    Tile(9, 2, 2, 0), Tile(10, 4, 2, 1), Tile(11, 2, 2, 2), Tile(12, 4, 2, 3),
                    Tile(13, 4, 3, 0), Tile(14, 2, 3, 1), Tile(15, 4, 3, 2), Tile(16, 2, 3, 3)
                )
                viewModel.setDebugState(
                    GameState(
                        boardSize = BoardSize.CLASSIC,
                        gameMode = GameMode.CLASSIC,
                        tiles = fullTiles,
                        score = 16820L,
                        bestScore = 24360L,
                        movesCount = 275,
                        isGameOver = true,
                        isVictory = false
                    )
                )
            }
            "OPEN_SETTINGS" -> {
                showSettingsState = true
            }
            "CLOSE_SETTINGS" -> {
                showSettingsState = false
            }
            "SET_THEME_DARK" -> {
                viewModel.setThemeMode(ThemeMode.DARK)
            }
            "SET_THEME_LIGHT" -> {
                viewModel.setThemeMode(ThemeMode.LIGHT)
            }
            "RESET_NORMAL" -> {
                showSettingsState = false
                viewModel.startNewGame(BoardSize.CLASSIC, GameMode.CLASSIC)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                viewModel.onMove(MoveDirection.UP)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                viewModel.onMove(MoveDirection.DOWN)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> {
                viewModel.onMove(MoveDirection.LEFT)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> {
                viewModel.onMove(MoveDirection.RIGHT)
                true
            }
            KeyEvent.KEYCODE_U -> {
                viewModel.onUndo()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
