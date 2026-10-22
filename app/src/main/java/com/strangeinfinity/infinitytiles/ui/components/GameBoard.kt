package com.strangeinfinity.infinitytiles.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.MoveDirection
import com.strangeinfinity.infinitytiles.ui.theme.DarkBoardBackground
import com.strangeinfinity.infinitytiles.ui.theme.DarkEmptyCell
import com.strangeinfinity.infinitytiles.ui.theme.LightBoardBackground
import com.strangeinfinity.infinitytiles.ui.theme.LightEmptyCell
import kotlin.math.abs

@Composable
fun GameBoard(
    gameState: GameState,
    onMove: (MoveDirection) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val dim = gameState.dimension
    val boardBg = if (isDark) DarkBoardBackground else LightBoardBackground
    val emptyCellBg = if (isDark) DarkEmptyCell else LightEmptyCell

    val density = LocalDensity.current
    val minSwipePx = with(density) { 20.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(boardBg)
            .padding(10.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var accumulatedX = 0f
                    var accumulatedY = 0f
                    var triggered = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        if (!triggered) {
                            val delta = change.positionChange()
                            accumulatedX += delta.x
                            accumulatedY += delta.y

                            val absX = abs(accumulatedX)
                            val absY = abs(accumulatedY)

                            if (absX >= minSwipePx || absY >= minSwipePx) {
                                triggered = true
                                change.consume()
                                if (absX > absY) {
                                    if (accumulatedX > 0f) {
                                        onMove(MoveDirection.RIGHT)
                                    } else {
                                        onMove(MoveDirection.LEFT)
                                    }
                                } else {
                                    if (accumulatedY > 0f) {
                                        onMove(MoveDirection.DOWN)
                                    } else {
                                        onMove(MoveDirection.UP)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val spacing = 8.dp
            val totalSpacing = spacing * (dim - 1)
            val cellSize = (maxWidth - totalSpacing) / dim

            // Draw empty background grid slots
            for (r in 0 until dim) {
                for (c in 0 until dim) {
                    val xOffset = (cellSize + spacing) * c
                    val yOffset = (cellSize + spacing) * r

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(cellSize)
                            .clip(RoundedCornerShape(10.dp))
                            .background(emptyCellBg)
                    )
                }
            }

            // Draw tiles with smooth, high-performance positional animation
            for (tile in gameState.tiles) {
                key(tile.id) {
                    val targetX = (cellSize + spacing) * tile.col
                    val targetY = (cellSize + spacing) * tile.row

                    val animatedX by animateDpAsState(
                        targetValue = targetX,
                        animationSpec = tween(
                            durationMillis = 110,
                            easing = FastOutSlowInEasing
                        ),
                        label = "tile_x_${tile.id}"
                    )
                    val animatedY by animateDpAsState(
                        targetValue = targetY,
                        animationSpec = tween(
                            durationMillis = 110,
                            easing = FastOutSlowInEasing
                        ),
                        label = "tile_y_${tile.id}"
                    )

                    TileView(
                        tile = tile,
                        isDark = isDark,
                        modifier = Modifier
                            .offset {
                                IntOffset(animatedX.roundToPx(), animatedY.roundToPx())
                            }
                            .size(cellSize)
                    )
                }
            }
        }
    }
}
