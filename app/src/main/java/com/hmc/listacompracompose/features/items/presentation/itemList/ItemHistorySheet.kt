package com.hmc.listacompracompose.features.items.presentation.itemList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hmc.listacompracompose.app.util.formatDate
import com.hmc.listacompracompose.app.util.formatPrice
import com.hmc.listacompracompose.app.util.formatRelativeDate
import com.hmc.listacompracompose.app.util.utcMidnightToLocalDayMillis
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHistorySheet(
    detail: ItemDetailState,
    onDismiss: () -> Unit,
    onAddPrice: (price: Double, date: Date) -> Unit,
    onEditItem: (ItemModel) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val history = detail.history

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = detail.item.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onEditItem(detail.item) }) {
                    Text("Editar")
                }
            }
            Text(
                text = if (detail.history.isNotEmpty()) {
                    "Último: ${detail.history.first().price.formatPrice()}"
                } else {
                    "Precio habitual: ${detail.item.price.formatPrice()}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (detail.history.isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.size(12.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Registrar precio pagado", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = if (history.isEmpty()) "Histórico de precios"
                else "Histórico de precios (${history.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (history.isEmpty()) {
                Text(
                    text = "Aún no hay precios registrados.\nEl primero se guardará al pulsar el botón.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    itemsIndexed(items = history, key = { _, entry -> entry.id }) { index, entry ->
                        PriceHistoryRow(
                            entry = entry,
                            previous = history.getOrNull(index + 1)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPriceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { price, date ->
                onAddPrice(price, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PriceHistoryRow(entry: PriceHistoryModel, previous: PriceHistoryModel?) {
    val diff = previous?.let { entry.price - it.price }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = entry.recordedAt.formatRelativeDate(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = entry.recordedAt.formatDate(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = entry.price.formatPrice(),
                style = MaterialTheme.typography.titleMedium
            )
            if (diff != null && diff != 0.0) {
                Text(
                    text = "%+.2f €".format(diff),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (diff > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPriceDialog(
    onDismiss: () -> Unit,
    onConfirm: (price: Double, date: Date) -> Unit
) {
    var priceText by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val price = priceText.replace(',', '.').toDoubleOrNull()
    val priceInvalid = priceText.isNotBlank() && (price == null || price < 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar precio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio pagado") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceInvalid,
                    supportingText = { if (priceInvalid) Text("Introduce un precio válido") }
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(Date(dateMillis).formatRelativeDate())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = price != null && !priceInvalid,
                onClick = { price?.let { onConfirm(it, Date(dateMillis)) } }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateMillis = utcMidnightToLocalDayMillis(it)
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}