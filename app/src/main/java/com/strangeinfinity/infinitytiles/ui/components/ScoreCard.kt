package com.strangeinfinity.infinitytiles.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangeinfinity.infinitytiles.ui.theme.DarkBoardBackground
import com.strangeinfinity.infinitytiles.ui.theme.DarkTextSecondary
import com.strangeinfinity.infinitytiles.ui.theme.LightBoardBackground
import com.strangeinfinity.infinitytiles.ui.theme.LightTextSecondary

@Composable
fun ScoreCard(
    label: String,
    value: Long,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val bg = if (isDark) DarkBoardBackground else LightBoardBackground
    val labelColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(
        modifier = modifier
            .widthIn(min = 72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            letterSpacing = 0.5.sp
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                slideInVertically { height -> height } togetherWith
                        slideOutVertically { height -> -height }
            },
            label = "score_anim_$label"
        ) { targetVal ->
            Text(
                text = targetVal.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
