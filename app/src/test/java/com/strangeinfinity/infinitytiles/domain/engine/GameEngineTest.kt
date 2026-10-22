package com.strangeinfinity.infinitytiles.domain.engine

import com.google.common.truth.Truth.assertThat
import com.strangeinfinity.infinitytiles.domain.model.BoardSize
import com.strangeinfinity.infinitytiles.domain.model.GameMode
import com.strangeinfinity.infinitytiles.domain.model.GameState
import com.strangeinfinity.infinitytiles.domain.model.MoveDirection
import com.strangeinfinity.infinitytiles.domain.model.Tile
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        // Use fixed seed for deterministic behavior in tests
        engine = GameEngine(random = Random(42))
    }

    private fun createTilesFromMatrix(matrix: Array<LongArray>): List<Tile> {
        val tiles = mutableListOf<Tile>()
        var id = 100L
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                val value = matrix[r][c]
                if (value > 0) {
                    tiles.add(Tile(id = id++, value = value, row = r, col = c))
                }
            }
        }
        return tiles
    }

    @Test
    fun `example A - four twos merge into two fours and score increases by 8`() {
        // Row: [2, 2, 2, 2] -> Left -> [4, 4, 0, 0]
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 0),
            Tile(id = 2, value = 2, row = 0, col = 1),
            Tile(id = 3, value = 2, row = 0, col = 2),
            Tile(id = 4, value = 2, row = 0, col = 3)
        )

        val result = engine.processMove(initialTiles, 4, MoveDirection.LEFT)

        assertThat(result.moved).isTrue()
        assertThat(result.scoreGained).isEqualTo(8L)
        assertThat(result.mergesCount).isEqualTo(2)

        val row0 = result.tiles.filter { it.row == 0 }.sortedBy { it.col }
        assertThat(row0.size).isEqualTo(2)
        assertThat(row0[0].col).isEqualTo(0)
        assertThat(row0[0].value).isEqualTo(4L)
        assertThat(row0[1].col).isEqualTo(1)
        assertThat(row0[1].value).isEqualTo(4L)
    }

    @Test
    fun `example B - four four eight zero does not chain merge into 16`() {
        // Row: [4, 4, 8, 0] -> Left -> [8, 8, 0, 0]
        val initialTiles = listOf(
            Tile(id = 1, value = 4, row = 0, col = 0),
            Tile(id = 2, value = 4, row = 0, col = 1),
            Tile(id = 3, value = 8, row = 0, col = 2)
        )

        val result = engine.processMove(initialTiles, 4, MoveDirection.LEFT)

        assertThat(result.moved).isTrue()
        assertThat(result.scoreGained).isEqualTo(8L)
        assertThat(result.mergesCount).isEqualTo(1)

        val row0 = result.tiles.filter { it.row == 0 }.sortedBy { it.col }
        assertThat(row0.size).isEqualTo(2)
        assertThat(row0[0].col).isEqualTo(0)
        assertThat(row0[0].value).isEqualTo(8L)
        assertThat(row0[1].col).isEqualTo(1)
        assertThat(row0[1].value).isEqualTo(8L)
    }

    @Test
    fun `movement with gaps - shifts tiles properly`() {
        // Row: [2, 0, 0, 2] -> Left -> [4, 0, 0, 0]
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 0),
            Tile(id = 2, value = 2, row = 0, col = 3)
        )

        val result = engine.processMove(initialTiles, 4, MoveDirection.LEFT)
        assertThat(result.moved).isTrue()
        assertThat(result.scoreGained).isEqualTo(4L)

        val row0 = result.tiles.filter { it.row == 0 }
        assertThat(row0.size).isEqualTo(1)
        assertThat(row0[0].col).isEqualTo(0)
        assertThat(row0[0].value).isEqualTo(4L)
    }

    @Test
    fun `movement right - shifts and merges toward right edge`() {
        // Row: [2, 0, 2, 4] -> Right -> [0, 0, 4, 4]
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 0),
            Tile(id = 2, value = 2, row = 0, col = 2),
            Tile(id = 3, value = 4, row = 0, col = 3)
        )

        val result = engine.processMove(initialTiles, 4, MoveDirection.RIGHT)
        assertThat(result.moved).isTrue()
        assertThat(result.scoreGained).isEqualTo(4L)

        val row0 = result.tiles.filter { it.row == 0 }.sortedBy { it.col }
        assertThat(row0.size).isEqualTo(2)
        assertThat(row0[0].col).isEqualTo(2)
        assertThat(row0[0].value).isEqualTo(4L)
        assertThat(row0[1].col).isEqualTo(3)
        assertThat(row0[1].value).isEqualTo(4L)
    }

    @Test
    fun `movement up and down - vertical columns merge correctly`() {
        // Col 1: [2, 2, 4, 8]^T -> Up -> [4, 4, 8, 0]^T
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 1),
            Tile(id = 2, value = 2, row = 1, col = 1),
            Tile(id = 3, value = 4, row = 2, col = 1),
            Tile(id = 4, value = 8, row = 3, col = 1)
        )

        val upResult = engine.processMove(initialTiles, 4, MoveDirection.UP)
        assertThat(upResult.moved).isTrue()
        assertThat(upResult.scoreGained).isEqualTo(4L)

        val col1Up = upResult.tiles.filter { it.col == 1 }.sortedBy { it.row }
        assertThat(col1Up.map { it.value }).containsExactly(4L, 4L, 8L).inOrder()

        val downResult = engine.processMove(initialTiles, 4, MoveDirection.DOWN)
        assertThat(downResult.moved).isTrue()
        val col1Down = downResult.tiles.filter { it.col == 1 }.sortedBy { it.row }
        // Col: 2, 2, 4, 8 -> Down: 2, 2 at top, 4, 8 at bottom -> 2 and 2 merge into 4
        // Result top to bottom: row 1: 4, row 2: 4, row 3: 8
        assertThat(col1Down.map { it.value }).containsExactly(4L, 4L, 8L).inOrder()
    }

    @Test
    fun `invalid move that does not change board - movesCount and score unchanged`() {
        val initialTiles = listOf(
            Tile(id = 1, value = 4, row = 0, col = 0),
            Tile(id = 2, value = 8, row = 1, col = 0)
        )
        val state = GameState(
            boardSize = BoardSize.CLASSIC,
            tiles = initialTiles,
            score = 100L,
            movesCount = 5
        )

        // Moving LEFT when all tiles are already at column 0 with no merges possible
        val afterMove = engine.move(state, MoveDirection.LEFT)

        assertThat(afterMove.score).isEqualTo(100L)
        assertThat(afterMove.movesCount).isEqualTo(5)
        assertThat(afterMove.tiles.size).isEqualTo(2)
        assertThat(afterMove.previousSnapshot).isNull()
    }

    @Test
    fun `valid move spawns exactly one new tile and increases movesCount`() {
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 0),
            Tile(id = 2, value = 2, row = 0, col = 1)
        )
        val state = GameState(
            boardSize = BoardSize.CLASSIC,
            tiles = initialTiles,
            score = 0L,
            movesCount = 0
        )

        val nextState = engine.move(state, MoveDirection.LEFT)

        assertThat(nextState.movesCount).isEqualTo(1)
        assertThat(nextState.score).isEqualTo(4L)
        // Started with 2 tiles, merged into 1, then spawned 1 = 2 total tiles
        assertThat(nextState.tiles.size).isEqualTo(2)
        assertThat(nextState.tiles.any { it.isNew }).isTrue()
        assertThat(nextState.previousSnapshot).isNotNull()
    }

    @Test
    fun `undo restores exact previous board, score, and moves`() {
        val initialTiles = listOf(
            Tile(id = 1, value = 2, row = 0, col = 0),
            Tile(id = 2, value = 2, row = 0, col = 1)
        )
        val state = GameState(
            boardSize = BoardSize.CLASSIC,
            tiles = initialTiles,
            score = 50L,
            bestScore = 100L,
            movesCount = 3
        )

        val movedState = engine.move(state, MoveDirection.LEFT)
        assertThat(movedState.score).isEqualTo(54L)
        assertThat(movedState.movesCount).isEqualTo(4)

        val restoredState = engine.undo(movedState)
        assertThat(restoredState.score).isEqualTo(50L)
        assertThat(restoredState.bestScore).isEqualTo(100L)
        assertThat(restoredState.movesCount).isEqualTo(3)
        assertThat(restoredState.tiles.map { it.value to (it.row to it.col) })
            .containsExactly(2L to (0 to 0), 2L to (0 to 1))
        assertThat(restoredState.previousSnapshot).isNull()
    }

    @Test
    fun `victory detection at 2048 and continue playing`() {
        val initialTiles = listOf(
            Tile(id = 1, value = 1024, row = 0, col = 0),
            Tile(id = 2, value = 1024, row = 0, col = 1)
        )
        val state = GameState(
            boardSize = BoardSize.CLASSIC,
            tiles = initialTiles,
            score = 0L
        )

        val stateAfterMerge = engine.move(state, MoveDirection.LEFT)
        assertThat(stateAfterMerge.isVictory).isTrue()

        // User clicks continue
        val continuedState = engine.continueAfterVictory(stateAfterMerge)
        assertThat(continuedState.isVictory).isFalse()
        assertThat(continuedState.hasContinuedAfterVictory).isTrue()

        // Subsequent move does not re-trigger victory
        val nextState = engine.move(continuedState, MoveDirection.DOWN)
        assertThat(nextState.isVictory).isFalse()
    }

    @Test
    fun `game over detection when no moves available`() {
        // Alternating full 4x4 grid:
        // [ 2,  4,  2,  4 ]
        // [ 4,  2,  4,  2 ]
        // [ 2,  4,  2,  4 ]
        // [ 4,  2,  4,  2 ]
        val fullMatrix = arrayOf(
            longArrayOf(2, 4, 2, 4),
            longArrayOf(4, 2, 4, 2),
            longArrayOf(2, 4, 2, 4),
            longArrayOf(4, 2, 4, 2)
        )
        val tiles = createTilesFromMatrix(fullMatrix)

        val hasMoves = engine.hasLegalMoves(tiles, 4)
        assertThat(hasMoves).isFalse()
    }

    @Test
    fun `full board with legal merge available is not game over`() {
        val fullMatrix = arrayOf(
            longArrayOf(2, 4, 2, 4),
            longArrayOf(4, 2, 4, 2),
            longArrayOf(2, 4, 2, 4),
            longArrayOf(4, 2, 4, 4) // row 3 col 2 and 3 both have 4
        )
        val tiles = createTilesFromMatrix(fullMatrix)

        val hasMoves = engine.hasLegalMoves(tiles, 4)
        assertThat(hasMoves).isTrue()
    }

    @Test
    fun `variable board sizes - 3x3 and 5x5`() {
        // Test 3x3
        val state3x3 = engine.startNewGame(BoardSize.SMALL)
        assertThat(state3x3.dimension).isEqualTo(3)
        assertThat(state3x3.tiles.size).isEqualTo(2)

        // Test 5x5
        val state5x5 = engine.startNewGame(BoardSize.LARGE)
        assertThat(state5x5.dimension).isEqualTo(5)
        assertThat(state5x5.tiles.size).isEqualTo(2)
    }

    @Test
    fun `large tile values and overflow safety`() {
        // Verify values up to 131,072 and beyond work safely with Long
        val initialTiles = listOf(
            Tile(id = 1, value = 65536L, row = 0, col = 0),
            Tile(id = 2, value = 65536L, row = 0, col = 1)
        )
        val state = GameState(tiles = initialTiles)
        val moved = engine.move(state, MoveDirection.LEFT)

        assertThat(moved.highestTile()).isEqualTo(131072L)
        assertThat(moved.score).isEqualTo(131072L)
    }

    @Test
    fun `deterministic random generator produces repeatable games`() {
        val engine1 = GameEngine(random = Random(12345))
        val engine2 = GameEngine(random = Random(12345))

        val g1 = engine1.startNewGame()
        val g2 = engine2.startNewGame()

        assertThat(g1.tiles.map { it.value to (it.row to it.col) })
            .isEqualTo(g2.tiles.map { it.value to (it.row to it.col) })

        val g1After = engine1.move(g1, MoveDirection.LEFT)
        val g2After = engine2.move(g2, MoveDirection.LEFT)

        assertThat(g1After.tiles.map { it.value to (it.row to it.col) })
            .isEqualTo(g2After.tiles.map { it.value to (it.row to it.col) })
    }
}
