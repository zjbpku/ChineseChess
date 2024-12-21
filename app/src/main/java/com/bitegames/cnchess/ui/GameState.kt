package com.bitegames.cnchess.ui

import ChessPiece
import com.bitegames.cnchess.model.Move
import kotlin.math.abs

data class GameState(
    val pieces: List<ChessPiece> = initializePieces(),
    val selectedPiece: ChessPiece? = null,
    val isRedTurn: Boolean = true,
    val moveHistory: List<Move> = emptyList(),
    val winner: Boolean? = null
) {
    fun selectPiece(piece: ChessPiece): GameState {
        return if (piece.isRed == isRedTurn) {
            copy(selectedPiece = piece)
        } else this
    }

    fun movePiece(toX: Int, toY: Int): GameState {
        val piece = selectedPiece
        if (piece == null) {
            println("selectedPiece is null")
            return this
        }
        println("Moving piece: $piece to ($toX, $toY)")

        // Validate the move
        if (!isValidMove(piece, toX, toY)) {
            println("Invalid move detected")
            return this
        }

        println("Move is valid, executing...")

        // Create new piece list
        val newPieces = pieces.toMutableList()

        // Check for captured piece
        val capturedPiece = pieces.find { it.x == toX && it.y == toY }

        // Remove captured piece if exists
        capturedPiece?.let {
            println("Capturing piece: $it")
            newPieces.remove(it)
        }

        // Find and update the moved piece
        val index = newPieces.indexOf(piece)
        if (index != -1) {
            val movedPiece = piece.copy(x = toX, y = toY)
            newPieces[index] = movedPiece
            println("Updated piece position: $movedPiece")
        } else {
            println("Error: Could not find piece in list")
            return this
        }

        println("Move completed. Turn changing from ${if (isRedTurn) "Red" else "Black"} to ${if (!isRedTurn) "Red" else "Black"}")
        return copy(
            pieces = newPieces,
            selectedPiece = null,
            isRedTurn = !isRedTurn,
            moveHistory = moveHistory + Move(piece, piece.x, piece.y, toX, toY, capturedPiece),
            winner = if (capturedPiece?.type == PieceType.KING) isRedTurn else null
        )
    }

    fun undoMove(): GameState {
        if (moveHistory.isEmpty()) {
            println("No moves to undo")
            return this
        }

        println("Undoing last move...")
        val lastMove = moveHistory.last()

        // Create new piece list
        val newPieces = pieces.toMutableList()

        // Find and restore the moved piece
        val movedPiece = newPieces.find {
            it.x == lastMove.toX && it.y == lastMove.toY
        }

        if (movedPiece == null) {
            println("Error: Could not find piece to undo")
            return this
        }

        // Restore piece to original position
        val restoredPiece = movedPiece.copy(x = lastMove.fromX, y = lastMove.fromY)
        val index = newPieces.indexOf(movedPiece)
        if (index != -1) {
            newPieces[index] = restoredPiece
            println("Restored piece to original position: $restoredPiece")
        }

        // Restore captured piece if any
        lastMove.capturedPiece?.let {
            newPieces.add(it)
            println("Restored captured piece: $it")
        }

        println("Undo completed. Turn changing from ${if (isRedTurn) "Red" else "Black"} to ${if (!isRedTurn) "Red" else "Black"}")

        return copy(
            pieces = newPieces,
            selectedPiece = null,
            isRedTurn = !isRedTurn,
            moveHistory = moveHistory.dropLast(1),
            winner = null
        )
    }

    fun isValidMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        println("Validating move for ${piece.type} from (${piece.x},${piece.y}) to ($newX,$newY)")

        // Basic validation
        if (newX !in 0..8 || newY !in 0..9) {
            println("Move outside board boundaries")
            return false
        }

        // Moving to same position is invalid
        if (piece.x == newX && piece.y == newY) {
            println("Invalid: Moving to same position")
            return false
        }

        // Check if target position has a friendly piece
        val targetPiece = pieces.find { it.x == newX && it.y == newY }
        if (targetPiece?.isRed == piece.isRed) {
            println("Cannot capture friendly piece")
            return false
        }

        // Validate move based on piece type
        val isValid = when (piece.type) {
            PieceType.CANNON -> {
                println("Checking cannon move...")
                val result = isValidCannonMove(piece, newX, newY)
                println("Cannon move validation result: $result")
                result
            }

            else -> when (piece.type) {
                PieceType.ROOK -> isValidRookMove(piece, newX, newY)
                PieceType.HORSE -> isValidHorseMove(piece, newX, newY)
                PieceType.ELEPHANT -> isValidElephantMove(piece, newX, newY)
                PieceType.ADVISOR -> isValidAdvisorMove(piece, newX, newY)
                PieceType.KING -> isValidKingMove(piece, newX, newY)
                PieceType.PAWN -> isValidPawnMove(piece, newX, newY)
                else -> false
            }
        }

        println("Final move validation result: $isValid")
        return isValid
    }

    private fun isValidRookMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        // Rook can only move horizontally or vertically
        if (piece.x != newX && piece.y != newY) return false

        // Check if path is clear
        if (piece.x == newX) {
            val minY = minOf(piece.y, newY)
            val maxY = maxOf(piece.y, newY)
            for (y in minY + 1 until maxY) {
                if (pieces.any { it.x == newX && it.y == y }) return false
            }
        } else {
            val minX = minOf(piece.x, newX)
            val maxX = maxOf(piece.x, newX)
            for (x in minX + 1 until maxX) {
                if (pieces.any { it.x == x && it.y == newY }) return false
            }
        }
        return true
    }

    private fun isValidHorseMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        val dx = abs(newX - piece.x)
        val dy = abs(newY - piece.y)

        // Horse moves in L-shape
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) return false

        // Check if horse is blocked
        val legX = if (dx == 2) piece.x + (newX - piece.x) / 2 else piece.x
        val legY = if (dy == 2) piece.y + (newY - piece.y) / 2 else piece.y

        return !pieces.any { it.x == legX && it.y == legY }
    }

    private fun isValidElephantMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        val dx = abs(newX - piece.x)
        val dy = abs(newY - piece.y)

        // Elephant moves diagonally by 2 points
        if (dx != 2 || dy != 2) return false

        // Elephant cannot cross river
        if (piece.isRed && newY < 5) return false
        if (!piece.isRed && newY > 4) return false

        // Check if elephant's eye is blocked
        val eyeX = piece.x + (newX - piece.x) / 2
        val eyeY = piece.y + (newY - piece.y) / 2

        return !pieces.any { it.x == eyeX && it.y == eyeY }
    }

    private fun isValidAdvisorMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        val dx = abs(newX - piece.x)
        val dy = abs(newY - piece.y)

        // Advisor moves diagonally by 1 point
        if (dx != 1 || dy != 1) return false

        // Must stay within palace
        if (newX !in 3..5) return false
        if (piece.isRed && newY !in 7..9) return false
        if (!piece.isRed && newY !in 0..2) return false

        return true
    }

    private fun isValidKingMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        val dx = abs(newX - piece.x)
        val dy = abs(newY - piece.y)

        // King moves one step orthogonally
        if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
            // Must stay within palace
            if (newX !in 3..5) return false
            if (piece.isRed && newY !in 7..9) return false
            if (!piece.isRed && newY !in 0..2) return false

            // Check for flying kings
            val oppositeKing = pieces.find {
                it.type == PieceType.KING && it.isRed != piece.isRed
            }
            if (oppositeKing != null && newX == oppositeKing.x) {
                val minY = minOf(newY, oppositeKing.y)
                val maxY = maxOf(newY, oppositeKing.y)
                if (!pieces.any { it.x == newX && it.y in (minY + 1) until maxY }) {
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun isValidCannonMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        println("Validating cannon move from (${piece.x},${piece.y}) to ($newX,$newY)")

        // Cannon can only move horizontally or vertically
        if (piece.x != newX && piece.y != newY) {
            println("Invalid: Not moving in straight line")
            return false
        }

        // Moving to same position is invalid
        if (piece.x == newX && piece.y == newY) {
            println("Invalid: Moving to same position")
            return false
        }

        var pieceCount = 0
        if (piece.x == newX) {  // Moving vertically
            val minY = minOf(piece.y, newY)
            val maxY = maxOf(piece.y, newY)
            println("Checking vertical path from y=$minY to y=$maxY")
            for (y in minY + 1 until maxY) {
                if (pieces.any { it.x == newX && it.y == y }) {
                    pieceCount++
                    println("Found piece in path at ($newX,$y)")
                }
            }
        } else {  // Moving horizontally
            val minX = minOf(piece.x, newX)
            val maxX = maxOf(piece.x, newX)
            println("Checking horizontal path from x=$minX to x=$maxX")
            for (x in minX + 1 until maxX) {
                if (pieces.any { it.x == x && it.y == newY }) {
                    pieceCount++
                    println("Found piece in path at ($x,$newY)")
                }
            }
        }

        // Check target position
        val hasTargetPiece = pieces.any { it.x == newX && it.y == newY }
        println("Target position has piece: $hasTargetPiece")

        // Cannon needs exactly one piece to jump over when capturing
        // and no pieces in path when moving to empty square
        val isValid = if (hasTargetPiece) {
            pieceCount == 1
        } else {
            pieceCount == 0
        }

        println("Cannon move validation: pieceCount=$pieceCount, hasTargetPiece=$hasTargetPiece, isValid=$isValid")
        return isValid
    }

    private fun isValidPawnMove(piece: ChessPiece, newX: Int, newY: Int): Boolean {
        val dx = abs(newX - piece.x)
        val dy = newY - piece.y

        if (piece.isRed) {
            // Can only move sideways after crossing river
            if (piece.y > 4 && (dx > 1 || dy >= 0)) return false
            if (piece.y <= 4 && dy > 0) return false
        } else {
            // Can only move sideways after crossing river
            if (piece.y < 5 && (dx > 1 || dy <= 0)) return false
            if (piece.y >= 5 && dy < 0) return false
        }

                // Can only move one step at a time
        if (dx > 1 || abs(dy) > 1) return false
        // Cannot move diagonally
        if (dx == 1 && abs(dy) == 1) return false

        return true
    }

    fun movePiece(piece: ChessPiece, newX: Int, newY: Int): GameState {
        if (isValidMove(piece, newX, newY)) {
            // Update the piece's position
            println("Moving piece: ${piece.type} from (${piece.x}, ${piece.y}) to ($newX, $newY)")
            piece.x = newX
            piece.y = newY

            // Switch turn
            return copy(
                selectedPiece = null,
                isRedTurn = !isRedTurn,
                moveHistory = moveHistory + Move(piece, piece.x, piece.y, newX, newY, null),
                winner = null // Update winner logic as needed
            )
        } else {
            println("Invalid move attempted for piece: ${piece.type}")
        }
        return this
    }

    companion object {
        fun initializePieces(): List<ChessPiece> {
            return buildList {
                // Black pieces (top)
                // First row (0th rank)
                add(ChessPiece(PieceType.ROOK, false, 0, 0))      // 車
                add(ChessPiece(PieceType.HORSE, false, 1, 0))     // 馬
                add(ChessPiece(PieceType.ELEPHANT, false, 2, 0))  // 象
                add(ChessPiece(PieceType.ADVISOR, false, 3, 0))   // 士
                add(ChessPiece(PieceType.KING, false, 4, 0))      // 將
                add(ChessPiece(PieceType.ADVISOR, false, 5, 0))   // 士
                add(ChessPiece(PieceType.ELEPHANT, false, 6, 0))  // 象
                add(ChessPiece(PieceType.HORSE, false, 7, 0))     // 馬
                add(ChessPiece(PieceType.ROOK, false, 8, 0))      // 車

                // Cannons (2nd rank)
                add(ChessPiece(PieceType.CANNON, false, 1, 2))    // 炮
                add(ChessPiece(PieceType.CANNON, false, 7, 2))    // 炮

                // Pawns (3rd rank)
                add(ChessPiece(PieceType.PAWN, false, 0, 3))      // 卒
                add(ChessPiece(PieceType.PAWN, false, 2, 3))      // 卒
                add(ChessPiece(PieceType.PAWN, false, 4, 3))      // 卒
                add(ChessPiece(PieceType.PAWN, false, 6, 3))      // 卒
                add(ChessPiece(PieceType.PAWN, false, 8, 3))      // 卒

                // Red pieces (bottom)
                // First row (9th rank)
                add(ChessPiece(PieceType.ROOK, true, 0, 9))       // 車
                add(ChessPiece(PieceType.HORSE, true, 1, 9))      // 馬
                add(ChessPiece(PieceType.ELEPHANT, true, 2, 9))   // 相
                add(ChessPiece(PieceType.ADVISOR, true, 3, 9))    // 仕
                add(ChessPiece(PieceType.KING, true, 4, 9))       // 帥
                add(ChessPiece(PieceType.ADVISOR, true, 5, 9))    // 仕
                add(ChessPiece(PieceType.ELEPHANT, true, 6, 9))   // 相
                add(ChessPiece(PieceType.HORSE, true, 7, 9))      // 馬
                add(ChessPiece(PieceType.ROOK, true, 8, 9))       // 車

                // Cannons (7th rank)
                add(ChessPiece(PieceType.CANNON, true, 1, 7))      // 炮
                add(ChessPiece(PieceType.CANNON, true, 7, 7))      // 炮

                // Pawns (6th rank)
                add(ChessPiece(PieceType.PAWN, true, 0, 6))        // 兵
                add(ChessPiece(PieceType.PAWN, true, 2, 6))        // 兵
                add(ChessPiece(PieceType.PAWN, true, 4, 6))        // 兵
                add(ChessPiece(PieceType.PAWN, true, 6, 6))        // 兵
                add(ChessPiece(PieceType.PAWN, true, 8, 6))        // 兵
            }
        }
    }
} 
