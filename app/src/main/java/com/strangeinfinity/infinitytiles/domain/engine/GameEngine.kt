package com.strangeinfinity.infinitytiles.domain.engine

import com.strangeinfinity.infinitytiles.domain.model.BoardSize
import com.strangeinfinity.infinitytiles.domain.model.GameMode
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.MoveDirection
import com.strangeinfinity.infinitytiles.domain.model.MoveResult
import com.strangeinfinity.infinitytiles.domain.model.Tile
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

class GameEngine(
    private var random: Random = Random.Default,
    private val probabilityOfFour: Float = 0.10f
) {
    private val idGenerator = AtomicLong(1L)

    fun setRandom(newRandom: Random) {
        this.random = newRandom
    }

    fun nextId(): Long = idGenerator.incrementAndGet()

    /**
     * Initializes a fresh game board with [boardSize] and spawns two initial tiles.
     */
    fun startNewGame(
        boardSize: BoardSize = BoardSize.CLASSIC,
        gameMode: GameMode = GameMode.CLASSIC,
        bestScore: Long = 0L
    ): GameState {
        val emptyState = GameState(
            boardSize = boardSize,
            gameMode = gameMode,
            tiles = emptyList(),
            score = 0L,
            bestScore = bestScore,
            movesCount = 0,
            isGameOver = false,
            isVictory = false,
            hasContinuedAfterVictory = false,
            previousSnapshot = null
        )

        // Spawn 2 initial tiles
        val withFirst = spawnRandomTile(emptyState) ?: emptyState
        val withSecond = spawnRandomTile(withFirst) ?: withFirst
        return withSecond
    }

    /**
     * Spawns a new tile on an empty cell.
     * Returns null if the board is full.
     */
    fun spawnRandomTile(currentState: GameState): GameState? {
        val dim = currentState.dimension
        val occupied = currentState.tiles.map { it.row to it.col }.toSet()
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (!occupied.contains(r to c)) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isEmpty()) return null

        val (row, col) = emptyCells[random.nextInt(emptyCells.size)]
        val value = if (random.nextFloat() < probabilityOfFour) 4L else 2L
        val newTile = Tile(
            id = nextId(),
            value = value,
            row = row,
            col = col,
            isNew = true,
            isMerged = false
        )

        val updatedTiles = currentState.tiles.map { it.copy(isNew = false, isMerged = false) } + newTile
        val gameOver = !hasLegalMoves(updatedTiles, dim)

        return currentState.copy(
            tiles = updatedTiles,
            isGameOver = gameOver
        )
    }

    /**
     * Executes a move in the given direction.
     * If the move does not change the board, no tiles are spawned, and the original state is returned.
     */
    fun move(currentState: GameState, direction: MoveDirection): GameState {
        if (currentState.isGameOver) return currentState

        val dim = currentState.dimension
        val currentTiles = currentState.tiles

        val moveResult = processMove(currentTiles, dim, direction)
        if (!moveResult.moved) {
            // No tiles moved or merged. State remains identical.
            return currentState
        }

        val snapshot = currentState.toSnapshot()
        val newScore = currentState.score + moveResult.scoreGained
        val newBestScore = maxOf(currentState.bestScore, newScore)
        val newMovesCount = currentState.movesCount + 1

        val stateAfterMerge = currentState.copy(
            tiles = moveResult.tiles,
            score = newScore,
            bestScore = newBestScore,
            movesCount = newMovesCount,
            previousSnapshot = snapshot
        )

        // Spawn a new tile after a valid move
        val stateAfterSpawn = spawnRandomTile(stateAfterMerge) ?: stateAfterMerge

        // Check victory (reached 2048)
        val reached2048 = stateAfterSpawn.tiles.any { it.value >= 2048L }
        val isVictory = reached2048 && !currentState.hasContinuedAfterVictory

        // Check game over
        val isGameOver = !hasLegalMoves(stateAfterSpawn.tiles, dim)

        return stateAfterSpawn.copy(
            isVictory = isVictory,
            isGameOver = isGameOver
        )
    }

    /**
     * Processes board movements and merges along the specified direction.
     */
    fun processMove(tiles: List<Tile>, dim: Int, direction: MoveDirection): MoveResult {
        var anyMoved = false
        var scoreGained = 0L
        var mergesCount = 0
        val resultTiles = mutableListOf<Tile>()

        when (direction) {
            MoveDirection.LEFT -> {
                for (r in 0 until dim) {
                    val line = tiles.filter { it.row == r }.sortedBy { it.col }
                    val lineResult = compressAndMergeLine(line) { idx -> r to idx }
                    if (lineResult.moved) anyMoved = true
                    scoreGained += lineResult.scoreGained
                    mergesCount += lineResult.mergesCount
                    resultTiles.addAll(lineResult.tiles)
                }
            }
            MoveDirection.RIGHT -> {
                for (r in 0 until dim) {
                    val line = tiles.filter { it.row == r }.sortedByDescending { it.col }
                    val lineResult = compressAndMergeLine(line) { idx -> r to (dim - 1 - idx) }
                    if (lineResult.moved) anyMoved = true
                    scoreGained += lineResult.scoreGained
                    mergesCount += lineResult.mergesCount
                    resultTiles.addAll(lineResult.tiles)
                }
            }
            MoveDirection.UP -> {
                for (c in 0 until dim) {
                    val line = tiles.filter { it.col == c }.sortedBy { it.row }
                    val lineResult = compressAndMergeLine(line) { idx -> idx to c }
                    if (lineResult.moved) anyMoved = true
                    scoreGained += lineResult.scoreGained
                    mergesCount += lineResult.mergesCount
                    resultTiles.addAll(lineResult.tiles)
                }
            }
            MoveDirection.DOWN -> {
                for (c in 0 until dim) {
                    val line = tiles.filter { it.col == c }.sortedByDescending { it.row }
                    val lineResult = compressAndMergeLine(line) { idx -> (dim - 1 - idx) to c }
                    if (lineResult.moved) anyMoved = true
                    scoreGained += lineResult.scoreGained
                    mergesCount += lineResult.mergesCount
                    resultTiles.addAll(lineResult.tiles)
                }
            }
        }

        val highest = resultTiles.maxOfOrNull { it.value } ?: 0L
        return MoveResult(
            moved = anyMoved,
            scoreGained = scoreGained,
            mergesCount = mergesCount,
            highestTileAchieved = highest,
            tiles = resultTiles
        )
    }

    private data class LineProcessResult(
        val tiles: List<Tile>,
        val moved: Boolean,
        val scoreGained: Long,
        val mergesCount: Int
    )

    private fun compressAndMergeLine(
        line: List<Tile>,
        targetPosProvider: (index: Int) -> Pair<Int, Int>
    ): LineProcessResult {
        if (line.isEmpty()) return LineProcessResult(emptyList(), false, 0L, 0)

        var moved = false
        var scoreGained = 0L
        var mergesCount = 0
        val outTiles = mutableListOf<Tile>()

        var i = 0
        var targetIndex = 0

        while (i < line.size) {
            val current = line[i]
            if (i + 1 < line.size && current.value == line[i + 1].value) {
                // Merge current and line[i+1]
                // Preserving the incoming tile's ID (next.id) allows Compose to smoothly animate its slide into target position!
                val next = line[i + 1]
                val mergedVal = current.value + next.value
                val (tRow, tCol) = targetPosProvider(targetIndex)
                outTiles.add(
                    Tile(
                        id = next.id,
                        value = mergedVal,
                        row = tRow,
                        col = tCol,
                        isNew = false,
                        isMerged = true
                    )
                )
                scoreGained += mergedVal
                mergesCount++
                moved = true
                i += 2
                targetIndex++
            } else {
                // Shift current without merging
                val (tRow, tCol) = targetPosProvider(targetIndex)
                if (current.row != tRow || current.col != tCol) {
                    moved = true
                }
                outTiles.add(current.moveTo(tRow, tCol))
                i++
                targetIndex++
            }
        }

        return LineProcessResult(outTiles, moved, scoreGained, mergesCount)
    }

    /**
     * Determines whether any legal moves remain on the board.
     */
    fun hasLegalMoves(tiles: List<Tile>, dim: Int): Boolean {
        if (tiles.size < dim * dim) return true

        // Grid is full; check adjacent matches
        val matrix = Array(dim) { LongArray(dim) }
        for (tile in tiles) {
            if (tile.row in 0 until dim && tile.col in 0 until dim) {
                matrix[tile.row][tile.col] = tile.value
            }
        }

        for (r in 0 until dim) {
            for (c in 0 until dim) {
                val current = matrix[r][c]
                if (current == 0L) return true
                // Check right neighbor
                if (c + 1 < dim && matrix[r][c + 1] == current) return true
                // Check bottom neighbor
                if (r + 1 < dim && matrix[r + 1][c] == current) return true
            }
        }
        return false
    }

    /**
     * Restores the game state from the previous snapshot.
     */
    fun undo(currentState: GameState): GameState {
        val snapshot = currentState.previousSnapshot ?: return currentState
        return currentState.restoreFromSnapshot(snapshot)
    }

    /**
     * Marks the victory dialog as acknowledged so the player can continue playing.
     */
    fun continueAfterVictory(currentState: GameState): GameState {
        return currentState.copy(
            isVictory = false,
            hasContinuedAfterVictory = true
        )
    }
}
