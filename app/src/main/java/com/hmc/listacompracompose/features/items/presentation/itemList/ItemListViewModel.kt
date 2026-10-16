package com.hmc.listacompracompose.features.items.presentation.itemList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.categories.domain.ObserveCategoriesUseCase
import com.hmc.listacompracompose.features.items.domain.DeleteItemUseCase
import com.hmc.listacompracompose.features.items.domain.InsertItemUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.items.domain.ObserveItemListUseCase
import com.hmc.listacompracompose.features.items.domain.UpdateItemUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ItemListUiState(
    val itemList: List<ItemModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@KoinViewModel
class ItemListViewModel(
    observeItemListUseCase: ObserveItemListUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val insertItemUseCase: InsertItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ItemListUiState> = combine(
        observeItemListUseCase().catch { e ->
            Log.e(TAG, "Error al observar el catálogo", e)
            _error.value = "No se pudo cargar el catálogo"
            emit(emptyList())
        },
        observeCategoriesUseCase().catch { e ->
            Log.e(TAG, "Error al observar las categorías", e)
            emit(emptyList())
        },
        _error
    ) { items, categories, error ->
        ItemListUiState(
            itemList = items,
            categories = categories,
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemListUiState(isLoading = true)
    )

    fun addItem(name: String, price: Double?, categoryId: Int?) =
        launchSafely("No se pudo guardar el producto") {
            val newItem = ItemModel(name = name.trim(), price = price, categoryId = categoryId)
            if (!insertItemUseCase(newItem)) {
                _error.value = "No se pudo guardar el producto"
            }
        }

    fun updateItem(item: ItemModel) = launchSafely("No se pudieron guardar los cambios") {
        updateItemUseCase(item)
    }

    fun deleteCatalogItem(item: ItemModel) = launchSafely("No se pudo eliminar el producto") {
        deleteItemUseCase(item)
    }

    fun errorShown() {
        _error.value = null
    }

    private fun launchSafely(errorMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, errorMessage, e)
                _error.value = errorMessage
            }
        }
    }

    private companion object {
        const val TAG = "ListaCompra"
    }
}