package com.hmc.listacompracompose.features.supermarkets.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveActiveSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveSupermarketsUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.SetActiveSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinViewModel

data class SupermarketSelectorUiState(
    val supermarkets: List<SupermarketModel> = emptyList(),
    val active: SupermarketModel? = null
)

@KoinViewModel
class SupermarketSelectorViewModel(
    observeSupermarketsUseCase: ObserveSupermarketsUseCase,
    observeActiveSupermarketUseCase: ObserveActiveSupermarketUseCase,
    private val setActiveSupermarketUseCase: SetActiveSupermarketUseCase
) : ViewModel() {

    val uiState: StateFlow<SupermarketSelectorUiState> = combine(
        observeSupermarketsUseCase(),
        observeActiveSupermarketUseCase()
    ) { supermarkets, active ->
        SupermarketSelectorUiState(supermarkets = supermarkets, active = active)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupermarketSelectorUiState()
    )

    fun select(id: Int) = setActiveSupermarketUseCase(id)
}

@Composable
fun SupermarketSelector(
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupermarketSelectorViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = state.active?.name ?: "Supermercado",
                style = MaterialTheme.typography.titleLarge
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar de supermercado")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.supermarkets.forEach { supermarket ->
                DropdownMenuItem(
                    text = { Text(supermarket.name) },
                    onClick = {
                        viewModel.select(supermarket.id)
                        expanded = false
                    },
                    trailingIcon = {
                        if (supermarket.id == state.active?.id) {
                            Icon(Icons.Default.Check, contentDescription = "Activo")
                        }
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Gestionar supermercados") },
                onClick = {
                    expanded = false
                    onManage()
                }
            )
        }
    }
}