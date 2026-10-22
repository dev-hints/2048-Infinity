package com.strangeinfinity.infinitytiles.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangeinfinity.infinitytiles.domain.model.Tile
import com.strangeinfinity.infinitytiles.ui.theme.TileColors

@Composable
fun TileView(
    tile: Tile,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val scaleAnim = remember(tile.id) {
        Animatable(if (tile.isNew) 0.0f else 1.0f)
    }

    LaunchedEffect(tile.id, tile.value, tile.isMerged) {
        if (tile.isNew) {
            scaleAnim.snapTo(0.0f)
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
            )
        } else if (tile.isMerged) {
            scaleAnim.snapTo(1.18f)
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val spec = remember(tile.value, isDark) {
        TileColors.forValue(tile.value, isDark)
    }

    val fontSize = when {
        tile.value < 100 -> 28.sp
        tile.value < 1000 -> 24.sp
        tile.value < 10000 -> 19.sp
        tile.value < 100000 -> 15.sp
        else -> 12.sp
    }

    val cornerRadius = 10.dp
    val shape = RoundedCornerShape(cornerRadius)

    val shadowElevation = if (tile.value >= 128) 4.dp else 1.dp

    Box(
        modifier = modifier
            .scale(scaleAnim.value)
            .shadow(shadowElevation, shape)
            .clip(shape)
            .background(spec.background)
            .then(
                if (tile.value >= 2048) {
                    Modifier.border(2.dp, Color(0xFFFDE047), shape)
                } else Modifier
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            color = spec.textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}
