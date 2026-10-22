package com.strangeinfinity.infinitytiles.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Accents
val BrandPrimary = Color(0xFF6366F1) // Indigo
val BrandSecondary = Color(0xFF38BDF8) // Sky Cyan
val BrandAccent = Color(0xFFF59E0B) // Amber Gold

// Backgrounds & Surfaces - Dark
val DarkBackground = Color(0xFF0A0E17)
val DarkSurface = Color(0xFF131B2A)
val DarkBoardBackground = Color(0xFF1E293B)
val DarkEmptyCell = Color(0xFF334155)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)

// Backgrounds & Surfaces - Light
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightBoardBackground = Color(0xFFCBD5E1)
val LightEmptyCell = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

data class TileColorSpec(
    val background: Color,
    val textColor: Color,
    val glowColor: Color? = null
)

object TileColors {
    fun forValue(value: Long, isDark: Boolean): TileColorSpec {
        return when (value) {
            2L -> if (isDark) {
                TileColorSpec(Color(0xFF2E384D), Color(0xFFE2E8F0))
            } else {
                TileColorSpec(Color(0xFFE2E8F0), Color(0xFF1E293B))
            }
            4L -> if (isDark) {
                TileColorSpec(Color(0xFF334155), Color(0xFFF1F5F9))
            } else {
                TileColorSpec(Color(0xFFCBD5E1), Color(0xFF0F172A))
            }
            8L -> TileColorSpec(Color(0xFFF59E0B), Color(0xFFFFFFFF))
            16L -> TileColorSpec(Color(0xFFEA580C), Color(0xFFFFFFFF))
            32L -> TileColorSpec(Color(0xFFE11D48), Color(0xFFFFFFFF))
            64L -> TileColorSpec(Color(0xFFDB2777), Color(0xFFFFFFFF))
            128L -> TileColorSpec(Color(0xFF7C3AED), Color(0xFFFFFFFF), Color(0x667C3AED))
            256L -> TileColorSpec(Color(0xFF4F46E5), Color(0xFFFFFFFF), Color(0x664F46E5))
            512L -> TileColorSpec(Color(0xFF2563EB), Color(0xFFFFFFFF), Color(0x662563EB))
            1024L -> TileColorSpec(Color(0xFF0284C7), Color(0xFFFFFFFF), Color(0x660284C7))
            2048L -> TileColorSpec(Color(0xFFD97706), Color(0xFFFFFFFF), Color(0x99F59E0B))
            4096L -> TileColorSpec(Color(0xFF059669), Color(0xFFFFFFFF), Color(0x9910B981))
            8192L -> TileColorSpec(Color(0xFF0D9488), Color(0xFFFFFFFF), Color(0x9914B8A6))
            16384L -> TileColorSpec(Color(0xFF9333EA), Color(0xFFFFFFFF), Color(0x99A855F7))
            else -> TileColorSpec(Color(0xFF4338CA), Color(0xFFFFFFFF), Color(0xCC6366F1))
        }
    }
}
