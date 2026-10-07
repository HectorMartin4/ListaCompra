package com.hmc.listacompracompose.features.items.presentation.itemList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.items.domain.DeleteItemUseCase
import com.hmc.listacompracompose.features.items.domain.GetItemListUseCase
import com.hmc.listacompracompose.features.items.domain.InsertItemUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ItemListUiState(
    val itemList: List<ItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@KoinViewModel
class ItemListViewModel(
    private val getItemListUseCase: GetItemListUseCase,
    private val insertItemUseCase: InsertItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemListUiState())
    val uiState: StateFlow<ItemListUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() = launchSafely("No se pudo cargar el catálogo") {
        _uiState.update { it.copy(isLoading = true) }
        try {
            refreshItems()
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addItem(name: String, price: Double) = launchSafely("No se pudo guardar el producto") {
        val newItem = ItemModel(
            name = name.trim() ,
            price = price
        )
        if (insertItemUseCase(newItem)) {
            refreshItems()
        } else {
            showError("No se pudo guardar el producto")
        }
    }

    fun deleteCatalogItem(item: ItemModel) = launchSafely("No se pudo eliminar el producto") {
        deleteItemUseCase(item) // si devuelve Boolean, compruébalo como en addItem
        refreshItems()
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    // Recarga la lista sin tocar isLoading, para que no desaparezca al añadir o borrar
    private suspend fun refreshItems() {
        val items = getItemListUseCase()
        _uiState.update { it.copy(itemList = items) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    private fun launchSafely(errorMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, errorMessage, e)
                showError(errorMessage)
            }
        }
    }

    private companion object {
        const val TAG = "ListaCompra"
    }
}