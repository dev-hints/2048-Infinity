package com.strangeinfinity.infinitytiles.domain.model

data class Tile(
    val id: Long,
    val value: Long,
    val row: Int,
    val col: Int,
    val isNew: Boolean = false,
    val isMerged: Boolean = false
) {
    fun moveTo(newRow: Int, newCol: Int): Tile = copy(
        row = newRow,
        col = newCol,
        isNew = false,
        isMerged = false
    )

    fun mergeWith(other: Tile, newId: Long): Tile = Tile(
        id = newId,
        value = this.value + other.value,
        row = other.row,
        col = other.col,
        isNew = false,
        isMerged = true
    )
}
