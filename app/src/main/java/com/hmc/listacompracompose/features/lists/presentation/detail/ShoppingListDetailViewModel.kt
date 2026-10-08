package com.hmc.listacompracompose.features.lists.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.items.domain.GetItemListUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.lists.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

data class ListDetailUiState(
    val listName: String = "",
    val items: List<ListItemModel> = emptyList(),
    val pickerItems: List<ItemModel>? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val total: Double get() = items.sumOf { it.price * it.amount }
}

@KoinViewModel
class ShoppingListDetailViewModel(
    @InjectedParam private val listId: Int,
    observeListsUseCase: ObserveShoppingListsUseCase,
    observeListItemsUseCase: ObserveListItemsUseCase,
    private val getItemListUseCase: GetItemListUseCase,
    private val addItemToListUseCase: AddItemToListUseCase,
    private val removeItemFromListUseCase: RemoveItemFromListUseCase,
    private val setItemCheckedUseCase: SetItemCheckedUseCase,
    private val setItemAmountUseCase: SetItemAmountUseCase
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<ItemModel>?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ListDetailUiState> = combine(
        observeListsUseCase(),
        observeListItemsUseCase(listId).catch { e ->
            Log.e(TAG, "Error al observar los productos de la lista", e)
            _error.value = "No se pudo cargar la lista"
            emit(emptyList())
        },
        _catalog,
        _error
    ) { lists, items, catalog, error ->
        val idsInList = items.map { it.itemId }.toSet()
        ListDetailUiState(
            listName = lists.firstOrNull { it.id == listId }?.name.orEmpty(),
            items = items,
            pickerItems = catalog?.filter { it.id !in idsInList },
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListDetailUiState()
    )

    fun openPicker() = launchSafely("No se pudo cargar el catálogo") {
        _catalog.value = getItemListUseCase()
    }

    fun closePicker() {
        _catalog.value = null
    }

    fun addItem(itemId: Int) = launchSafely("No se pudo añadir el producto") {
        addItemToListUseCase(listId, itemId)
    }

    fun removeItem(itemId: Int) = launchSafely("No se pudo quitar el producto") {
        removeItemFromListUseCase(listId, itemId)
    }

    fun setChecked(itemId: Int, checked: Boolean) = launchSafely("No se pudo actualizar el producto") {
        setItemCheckedUseCase(listId, itemId, checked)
    }

    fun setAmount(itemId: Int, amount: Int) = launchSafely("No se pudo cambiar la cantidad") {
        setItemAmountUseCase(listId, itemId, amount)
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