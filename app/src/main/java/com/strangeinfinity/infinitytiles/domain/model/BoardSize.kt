package com.strangeinfinity.infinitytiles.domain.model

enum class BoardSize(val dimension: Int, val displayName: String) {
    SMALL(3, "3 × 3"),
    CLASSIC(4, "4 × 4"),
    LARGE(5, "5 × 5");

    val cellCount: Int get() = dimension * dimension

    companion object {
        fun fromDimension(dim: Int): BoardSize = when (dim) {
            3 -> SMALL
            5 -> LARGE
            else -> CLASSIC
        }
    }
}
