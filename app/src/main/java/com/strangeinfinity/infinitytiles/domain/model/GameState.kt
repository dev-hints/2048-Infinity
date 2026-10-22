package com.strangeinfinity.infinitytiles.domain.model

data class GameSnapshot(
    val tiles: List<Tile>,
    val score: Long,
    val movesCount: Int,
    val isGameOver: Boolean,
    val isVictory: Boolean,
    val hasContinuedAfterVictory: Boolean
)

data class GameState(
    val boardSize: BoardSize = BoardSize.CLASSIC,
    val gameMode: GameMode = GameMode.CLASSIC,
    val tiles: List<Tile> = emptyList(),
    val score: Long = 0L,
    val bestScore: Long = 0L,
    val movesCount: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val hasContinuedAfterVictory: Boolean = false,
    val previousSnapshot: GameSnapshot? = null,
    val timeLeftSeconds: Int? = null
) {
    val dimension: Int get() = boardSize.dimension

    fun getTileAt(row: Int, col: Int): Tile? =
        tiles.firstOrNull { it.row == row && it.col == col }

    fun highestTile(): Long =
        tiles.maxOfOrNull { it.value } ?: 0L

    fun toSnapshot(): GameSnapshot = GameSnapshot(
        tiles = tiles.map { it.copy(isNew = false, isMerged = false) },
        score = score,
        movesCount = movesCount,
        isGameOver = isGameOver,
        isVictory = isVictory,
        hasContinuedAfterVictory = hasContinuedAfterVictory
    )

    fun restoreFromSnapshot(snapshot: GameSnapshot): GameState = copy(
        tiles = snapshot.tiles,
        score = snapshot.score,
        movesCount = snapshot.movesCount,
        isGameOver = snapshot.isGameOver,
        isVictory = snapshot.isVictory,
        hasContinuedAfterVictory = snapshot.hasContinuedAfterVictory,
        previousSnapshot = null
    )

    /**
     * Converts the tiles list into a 2D Array<LongArray> for easy testing and debugging.
     */
    fun toMatrix(): Array<LongArray> {
        val dim = boardSize.dimension
        val matrix = Array(dim) { LongArray(dim) }
        for (tile in tiles) {
            if (tile.row in 0 until dim && tile.col in 0 until dim) {
                matrix[tile.row][tile.col] = tile.value
            }
        }
        return matrix
    }
}
