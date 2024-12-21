package com.bitegames.cnchess.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.min

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    gameState: GameState,
    onMove: (GameState) -> Unit
) {
    var boardSize by remember { mutableFloatStateOf(0f) }
    val gridSize = boardSize / 9
    var selectedPiece by remember { mutableStateOf<ChessPiece?>(null) }
    var rememberedGameState by remember { mutableStateOf(gameState) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDEB887))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val startX = size.width / 10
                    val gridWidth = (8 * size.width / 10) / 8
                    val gridHeight = size.height / 9

                    // Calculate the grid position based on the click
                    val x = ((offset.x - startX) / gridWidth).toInt()
                    val y = ((offset.y) / gridHeight).toInt()
                    val boardY = 9 - y

                    // Check if the click is within the bounds of the board
                    if (x in 0..8 && boardY in 0..9) {
                        // Check if the click is near any piece
                        val touchedPiece = rememberedGameState.pieces.find {
                            // Calculate the position of the piece
                            val pieceX = startX + it.x * gridWidth
                            val pieceY = (9 - it.y) * gridSize
                            // Calculate the distance from the click to the piece
                            val distance = ((offset.x - pieceX) * (offset.x - pieceX) + (offset.y - pieceY) * (offset.y - pieceY)).toDouble()
                            // Check if the distance is within a certain radius (e.g., half the grid width)
                            distance <= (gridWidth / 2) * (gridWidth / 2) // Adjust the radius as needed
                        }

                        // First click - select piece
                        if (selectedPiece == null) {
                            if (touchedPiece?.isRed == rememberedGameState.isRedTurn) {
                                selectedPiece = touchedPiece
                                rememberedGameState = rememberedGameState.selectPiece(touchedPiece)
                                onMove(rememberedGameState)

                                // If the selected piece is black, let the AI make a move
                                if (!touchedPiece.isRed) {
                                    val aiMove = getRandomMove(rememberedGameState)
                                    aiMove?.let {
                                        rememberedGameState = rememberedGameState.movePiece(it.piece, it.toX, it.toY)
                                        onMove(rememberedGameState) // Update the game state after AI move
                                    }
                                }
                            }
                        }
                        // Second click - move piece or select new piece
                        else {
                            if (touchedPiece == selectedPiece) {
                                // Unselect if clicking the same piece
                                selectedPiece = null
                                rememberedGameState = rememberedGameState.copy(selectedPiece = null)
                                onMove(rememberedGameState)
                            } else if (touchedPiece?.isRed == rememberedGameState.isRedTurn) {
                                // Select new piece if clicking another friendly piece
                                selectedPiece = touchedPiece
                                rememberedGameState = rememberedGameState.selectPiece(touchedPiece)
                                onMove(rememberedGameState)
                            } else {
                                // Try to move to the target position
                                val moveResult = rememberedGameState.movePiece(x, boardY)
                                if (moveResult != rememberedGameState) {
                                    selectedPiece = null  // Clear selection after successful move
                                    rememberedGameState = moveResult
                                    onMove(rememberedGameState)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .onSizeChanged { size ->
                    boardSize = min(size.width, size.height).toFloat()
                }
        ) {
            drawBoard()
            drawRiver(gridSize, gridSize)
            drawPieces(rememberedGameState, gridSize)
            // Draw highlight for selected piece
            selectedPiece?.let { piece ->
                val startX = size.width / 10
                val gridWidth = (8 * size.width / 10) / 8
                val x = startX + piece.x * gridWidth
                val y = (9 - piece.y) * gridSize

                drawCircle(
                    color = Color(0x4400FF00),
                    radius = gridSize * 0.35f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private fun DrawScope.drawBoard() {
    // Draw board background to fill the entire canvas
    drawRect(Color(0xFFDEB887)) // Use the full size of the canvas

    val startX = size.width / 10  // Adjust starting position
    val endX = 9 * size.width / 10  // Adjust ending position
    val gridWidth = (endX - startX) / 8  // Calculate grid width

    // Draw horizontal lines
    for (i in 0..9) {
        drawLine(
            Color.Black,
            Offset(startX, i * size.height / 9),
            Offset(endX, i * size.height / 9),
            2f
        )
    }

    // Draw vertical lines
    for (i in 0..8) {
        val x = startX + i * gridWidth
        if (i == 0 || i == 8) {
            drawLine(
                Color.Black,
                Offset(x, 0f),
                Offset(x, size.height),
                2f
            )
        } else {
            drawLine(
                Color.Black,
                Offset(x, 0f),
                Offset(x, size.height / 9 * 4),
                2f
            )
            drawLine(
                Color.Black,
                Offset(x, size.height / 9 * 5f),
                Offset(x, size.height),
                2f
            )
        }
    }

    // Draw palace diagonal lines
    // Top palace (black side)
    drawLine(
        Color.Black,
        Offset(3 * gridWidth + startX, 0f),
        Offset(5 * gridWidth + startX, 2 * size.height / 9),
        2f
    )
    drawLine(
        Color.Black,
        Offset(5 * gridWidth + startX, 0f),
        Offset(3 * gridWidth + startX, 2 * size.height / 9),
        2f
    )

    // Bottom palace (red side)
    drawLine(
        Color.Black,
        Offset(3 * gridWidth + startX, 7 * size.height / 9),
        Offset(5 * gridWidth + startX, 9 * size.height / 9),
        2f
    )
    drawLine(
        Color.Black,
        Offset(5 * gridWidth + startX, 7 * size.height / 9),
        Offset(3 * gridWidth + startX, 9 * size.height / 9),
        2f
    )
}

private fun DrawScope.drawPieces(gameState: GameState, gridSize: Float) {
    val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = gridSize * 0.4f  // Reduce text size
        typeface = Typeface.DEFAULT_BOLD
    }

    val startX = size.width / 10  // Match board's starting position
    val gridWidth = (8 * size.width / 10) / 8  // Match board's grid width

    gameState.pieces.forEach { piece ->
        // Adjust piece position to align with board grid
        val x = startX + piece.x * gridWidth
        val y = (9 - piece.y) * gridSize
        val center = Offset(x, y)

        // Draw piece circle with smaller radius
        drawCircle(
            color = if (piece.isRed) Color.Red else Color.Black,
            radius = gridSize * 0.35f,  // Reduce piece size
            center = center
        )

        // Draw piece text
        drawIntoCanvas { canvas ->
            textPaint.color = Color.White.toArgb()

            // Calculate baseline for vertically centered text
            val fontMetrics = textPaint.fontMetrics
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val textOffset = textHeight / 2 - fontMetrics.bottom

            canvas.nativeCanvas.drawText(
                when (piece.type) {
                    PieceType.KING -> if (piece.isRed) "帥" else "將"
                    PieceType.ADVISOR -> if (piece.isRed) "仕" else "士"
                    PieceType.ELEPHANT -> if (piece.isRed) "相" else "象"
                    PieceType.HORSE -> if (piece.isRed) "馬" else "馬"
                    PieceType.ROOK -> if (piece.isRed) "車" else "車"
                    PieceType.CANNON -> if (piece.isRed) "炮" else "炮"
                    PieceType.PAWN -> if (piece.isRed) "兵" else "卒"
                },
                center.x,
                center.y + textOffset,
                textPaint
            )
        }
    }
}

// Draw the text "楚河漢界"
private fun DrawScope.drawRiver(gridWidth: Float, gridHeight: Float) {
    val gridSize = min(gridWidth, gridHeight)
    val textSizeFloat = gridSize * .6f

    // Save the current canvas state
    drawContext.canvas.nativeCanvas.save()

    // Rotate the canvas around the center of the text
    drawContext.canvas.nativeCanvas.rotate(
        270f,
        gridWidth * 2f,
        gridHeight * 4.5f
    )

    // Draw the rotated character "楚"
    drawContext.canvas.nativeCanvas.drawText(
        "楚",
        gridWidth * 2f,
        gridHeight * 4.5f, // Adjust the vertical position of the text
        Paint().apply {
            color = Color.Black.toArgb()
            textSize = textSizeFloat // Adjust text size
            textAlign = Paint.Align.CENTER
        }
    )

    // Draw the character "河"
    drawContext.canvas.nativeCanvas.drawText(
        "河",
        gridWidth * 2f,
        gridHeight * 5.5f, // Adjust the vertical position of the text
        Paint().apply {
            color = Color.Black.toArgb()
            textSize = textSizeFloat // Adjust text size
            textAlign = Paint.Align.CENTER
        }
    )

    // Restore the canvas state
    drawContext.canvas.nativeCanvas.restore()

    // Draw the character "漢"
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(
        90f,
        gridWidth * 7f,
        gridHeight * 4.5f
    )
    drawContext.canvas.nativeCanvas.drawText(
        "漢",
        gridWidth * 7f,
        gridHeight * 4.5f, // Adjust the vertical position of the text
        Paint().apply {
            color = Color.Black.toArgb()
            textSize = textSizeFloat // Adjust text size
            textAlign = Paint.Align.CENTER
        }
    )

    // Draw the character "界"
    drawContext.canvas.nativeCanvas.drawText(
        "界",
        gridWidth * 7f,
        gridHeight * 5.5f, // Adjust the vertical position of the text
        Paint().apply {
            color = Color.Black.toArgb()
            textSize = textSizeFloat // Adjust text size
            textAlign = Paint.Align.CENTER
        }
    )

    // Restore the canvas state
    drawContext.canvas.nativeCanvas.restore()
}