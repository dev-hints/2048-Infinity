package com.strangeinfinity.infinitytiles.domain.model

data class GameStats(
    val gamesPlayed: Long = 0L,
    val gamesWon: Long = 0L,
    val bestScore: Long = 0L,
    val highestTile: Long = 0L,
    val totalMerges: Long = 0L,
    val totalMoves: Long = 0L
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0

    val averageScore: Long
        get() = if (gamesPlayed > 0) bestScore / 2 else 0 // Reasonable indicator or tracked separately
}
