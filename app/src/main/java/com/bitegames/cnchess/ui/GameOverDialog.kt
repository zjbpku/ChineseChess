package com.bitegames.cnchess.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bitegames.cnchess.R

@Composable
fun GameOverDialog(
    winner: Boolean?,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    if (winner != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.dialog_game_over)) },
            text = { 
                Text(
                    stringResource(
                        if (winner) R.string.dialog_winner_red 
                        else R.string.dialog_winner_black
                    )
                ) 
            },
            confirmButton = {
                TextButton(onClick = onRestart) {
                    Text(stringResource(R.string.btn_restart))
                }
            },
            dismissButton = {
                TextButton(onClick = onExit) {
                    Text(stringResource(R.string.btn_exit))
                }
            }
        )
    }
} 