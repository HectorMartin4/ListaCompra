package com.hmc.listacompracompose.features.lists.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.categories.domain.ObserveCategoriesUseCase
import com.hmc.listacompracompose.features.items.domain.GetItemListUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.lists.domain.AddItemToListUseCase
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.domain.ObserveListItemsUseCase
import com.hmc.listacompracompose.features.lists.domain.ObserveShoppingListsUseCase
import com.hmc.listacompracompose.features.lists.domain.RemoveItemFromListUseCase
import com.hmc.listacompracompose.features.lists.domain.RestoreItemToListUseCase
import com.hmc.listacompracompose.features.lists.domain.SetItemAmountUseCase
import com.hmc.listacompracompose.features.lists.domain.SetItemCheckedUseCase
import com.hmc.listacompracompose.ui.components.UndoPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

data class ListDetailUiState(
    val listName: String = "",
    val items: List<ListItemModel> = emptyList(),
    val picker: PickerData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val undo: UndoPrompt? = null
) {
    val total: Double get() = items.sumOf { (it.price ?: 0.0) * it.amount }
    val itemsWithoutPrice: Int get() = items.count { it.price == null }
}

data class PickerData(
    val items: List<ItemModel>,
    val categories: List<CategoryModel>
)

@KoinViewModel
class ShoppingListDetailViewModel(
    @InjectedParam private val listId: Int,
    observeListsUseCase: ObserveShoppingListsUseCase,
    observeListItemsUseCase: ObserveListItemsUseCase,
    private val getItemListUseCase: GetItemListUseCase,
    private val addItemToListUseCase: AddItemToListUseCase,
    private val removeItemFromListUseCase: RemoveItemFromListUseCase,
    private val setItemCheckedUseCase: SetItemCheckedUseCase,
    private val setItemAmountUseCase: SetItemAmountUseCase,
    private val restoreItemToListUseCase: RestoreItemToListUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _picker = MutableStateFlow<PickerData?>(null)
    private val _error = MutableStateFlow<String?>(null)
    private val _undo = MutableStateFlow<UndoPrompt?>(null)
    private var lastRemoved: ListItemModel? = null

    val uiState: StateFlow<ListDetailUiState> = combine(
        observeListsUseCase(),
        observeListItemsUseCase(listId).catch { e ->
            Log.e(TAG, "Error al observar los productos de la lista", e)
            _error.value = "No se pudo cargar la lista"
            emit(emptyList())
        },
        _picker,
        _error,
        _undo
    ) { lists, items, picker, error, undo ->
        val idsInList = items.map { it.itemId }.toSet()
        ListDetailUiState(
            listName = lists.firstOrNull { it.id == listId }?.name.orEmpty(),
            items = items,
            picker = picker?.copy(items = picker.items.filter { it.id !in idsInList }),
            isLoading = false,
            error = error,
            undo = undo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListDetailUiState()
    )

    fun openPicker() = launchSafely("No se pudo cargar el catálogo") {
        _picker.value = PickerData(
            items = getItemListUseCase(),
            categories = observeCategoriesUseCase().first()
        )
    }

    fun closePicker() {
        _picker.value = null
    }

    fun addItem(itemId: Int) = launchSafely("No se pudo añadir el producto") {
        addItemToListUseCase(listId, itemId)
    }

    fun removeItem(item: ListItemModel) = launchSafely("No se pudo quitar el producto") {
        removeItemFromListUseCase(listId, item.itemId)
        lastRemoved = item
        _undo.value = UndoPrompt("\"${item.name}\" quitado de la lista")
    }

    fun undoRemove() = launchSafely("No se pudo deshacer") {
        lastRemoved?.let {
            restoreItemToListUseCase(listId, it.itemId, it.amount, it.isChecked)
        }
        lastRemoved = null
    }

    fun undoShown() {
        _undo.value = null
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