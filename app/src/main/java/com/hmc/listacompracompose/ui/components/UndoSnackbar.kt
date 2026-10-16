package com.hmc.listacompracompose.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

data class UndoPrompt(
    val message: String,
    val token: Long = System.nanoTime()
)

@Composable
fun UndoSnackbarEffect(
    prompt: UndoPrompt?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onPromptShown: () -> Unit
) {
    LaunchedEffect(prompt) {
        if (prompt != null) {
            val result = snackbarHostState.showSnackbar(
                message = prompt.message,
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) onUndo()
            onPromptShown()
        }
    }
}