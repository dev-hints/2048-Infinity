package com.strangeinfinity.infinitytiles.domain.model

data class MoveResult(
    val moved: Boolean,
    val scoreGained: Long,
    val mergesCount: Int,
    val highestTileAchieved: Long,
    val tiles: List<Tile>
)
