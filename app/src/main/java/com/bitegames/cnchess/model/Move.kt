package com.bitegames.cnchess.model


data class Move(
    val piece: ChessPiece,
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int,
    val capturedPiece: ChessPiece?
) 