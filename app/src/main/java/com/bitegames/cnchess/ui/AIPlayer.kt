package com.bitegames.cnchess.ui

import com.bitegames.cnchess.model.Move
import kotlin.random.Random

fun getRandomMove(gameState: GameState): Move? {
    val validMoves = mutableListOf<Move>()

    // Find all valid moves for the AI's pieces
    gameState.pieces.filter { !it.isRed }.forEach { piece ->
        for (x in 0..8) {
            for (y in 0..9) {
                if (gameState.isValidMove(piece, x, y)) {
                    validMoves.add(Move(piece, piece.x, piece.y, x, y, null))
                }
            }
        }
    }

    // Debugging output
    println("AI found ${validMoves.size} valid moves.")

    // Return a random valid move if available
    return if (validMoves.isNotEmpty()) {
        validMoves[Random.nextInt(validMoves.size)]
    } else {
        null
    }
} 