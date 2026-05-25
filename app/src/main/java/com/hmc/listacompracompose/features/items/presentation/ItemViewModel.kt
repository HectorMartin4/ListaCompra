package com.hmc.listacompracompose.features.items.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.items.domain.GetItemListUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ItemViewModel(
    private val getItemListUseCase: GetItemListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Loading)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            Log.d("AppListaCompra", "1. fetchItems invocado. Estado actual: Loading")
            _uiState.value = ItemUiState.Loading
            try {
                val items = getItemListUseCase()

                _uiState.value =
                    if (items.isEmpty()) {
                        ItemUiState.Empty
                    } else {
                        ItemUiState.Success(items)
                    }
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(e.message ?: "Error inesperado al cargar datos")
            }
        }
    }

}

sealed interface ItemUiState {
    object Loading : ItemUiState
    object Empty : ItemUiState
    data class Success(val items: List<ItemModel>) : ItemUiState
    data class Error(val message: String) : ItemUiState
}