package com.strangeinfinity.infinitytiles.domain.model

enum class GameMode(val displayName: String, val description: String) {
    CLASSIC("Classic", "Infinite puzzle with standard rules"),
    DAILY_CHALLENGE("Daily Challenge", "Shared deterministic board for today"),
    TIME_RUSH("Time Rush", "Score as many points as possible before time runs out");

    companion object {
        fun fromName(name: String): GameMode = try {
            valueOf(name)
        } catch (_: Exception) {
            CLASSIC
        }
    }
}
