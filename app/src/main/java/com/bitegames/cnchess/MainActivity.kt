package com.bitegames.cnchess

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bitegames.cnchess.ui.ChessBoard
import com.bitegames.cnchess.ui.GameOverDialog
import com.bitegames.cnchess.ui.GameState
import com.bitegames.cnchess.ui.theme.ChineseChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChineseChessTheme {
                ChessGame()
            }
        }
    }
}

@Composable
fun ChessGame() {
    var gameState by remember { mutableStateOf(GameState()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFDEB887)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChessBoard(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .weight(1f),
            gameState = gameState,
            onMove = { newState -> 
                gameState = newState // Update the game state when a move is made
            }
        )

        Button(
            onClick = { gameState = gameState.undoMove() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.btn_undo))
        }
    }

    if (gameState.winner != null) {
        GameOverDialog(
            winner = gameState.winner,
            onRestart = { gameState = GameState() },
            onExit = { (context as? Activity)?.finish() }
        )
    }
}

