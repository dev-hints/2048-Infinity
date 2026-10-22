package com.strangeinfinity.infinitytiles.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangeinfinity.infinitytiles.data.ThemeMode
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.ui.GameViewModel
import com.strangeinfinity.infinitytiles.ui.components.ControlBar
import com.strangeinfinity.infinitytiles.ui.components.GameBoard
import com.strangeinfinity.infinitytiles.ui.components.GameOverDialog
import com.strangeinfinity.infinitytiles.ui.components.RestartConfirmationDialog
import com.strangeinfinity.infinitytiles.ui.components.ScoreCard
import com.strangeinfinity.infinitytiles.ui.components.VictoryDialog

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    initialShowSettings: Boolean = false
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val gameStats by viewModel.gameStats.collectAsState()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    var showRestartDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember(initialShowSettings) { mutableStateOf(initialShowSettings) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun shareGame(state: GameState) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "I scored ${state.score} with a highest tile of ${state.highestTile()} in 2048-Infinity! Merge. Think. Go Beyond. #2048Infinity #StrangeInfinity"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Score"))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Title & Scores
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game Title & Infinity Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "2048-Infinity",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Merge. Think. Go Beyond.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Score Cards
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreCard(
                            label = "SCORE",
                            value = gameState.score,
                            isDark = isDark
                        )
                        ScoreCard(
                            label = "BEST",
                            value = gameState.bestScore,
                            isDark = isDark
                        )
                    }
                }

                // Mode & Board Size Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode: ${gameState.gameMode.displayName} (${gameState.boardSize.displayName})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Moves: ${gameState.movesCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Middle Section: Game Board
            GameBoard(
                gameState = gameState,
                onMove = { direction -> viewModel.onMove(direction) },
                isDark = isDark,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Bottom Section: Controls & Developer attribution
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ControlBar(
                    canUndo = gameState.previousSnapshot != null,
                    onUndo = { viewModel.onUndo() },
                    onRestart = { showRestartDialog = true },
                    onOpenStats = { showStatsDialog = true },
                    onOpenSettings = { showSettingsDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Swipe to merge tiles • Developed by StrangeInfinity",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Overlays and Dialogs
    if (showRestartDialog) {
        RestartConfirmationDialog(
            onConfirm = {
                viewModel.startNewGame()
                showRestartDialog = false
            },
            onDismiss = { showRestartDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentTheme = themeMode,
            onThemeChanged = { viewModel.setThemeMode(it) },
            soundEnabled = soundEnabled,
            onSoundChanged = { viewModel.setSoundEnabled(it) },
            hapticEnabled = hapticEnabled,
            onHapticChanged = { viewModel.setHapticEnabled(it) },
            currentBoardSize = gameState.boardSize,
            onBoardSizeChanged = { viewModel.changeBoardSize(it) },
            currentGameMode = gameState.gameMode,
            onGameModeChanged = { viewModel.changeGameMode(it) },
            onResetStats = { viewModel.resetStatistics() },
            onOpenAbout = {
                showSettingsDialog = false
                showAboutDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showStatsDialog) {
        StatisticsDialog(
            stats = gameStats,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    if (gameState.isVictory && !gameState.hasContinuedAfterVictory) {
        VictoryDialog(
            gameState = gameState,
            onContinue = { viewModel.continueAfterVictory() },
            onRestart = { viewModel.startNewGame() },
            onShare = { shareGame(gameState) }
        )
    }

    if (gameState.isGameOver) {
        GameOverDialog(
            gameState = gameState,
            canUndo = gameState.previousSnapshot != null,
            onUndo = { viewModel.onUndo() },
            onRestart = { viewModel.startNewGame() },
            onShare = { shareGame(gameState) }
        )
    }
}
