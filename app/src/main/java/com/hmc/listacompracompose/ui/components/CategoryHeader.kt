package com.hmc.listacompracompose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryHeader(
    name: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    Surface(color = containerColor, modifier = modifier.fillMaxWidth()) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}