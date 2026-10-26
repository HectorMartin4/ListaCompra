package com.hmc.listacompracompose.features.lists.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.categories.domain.ObserveCategoriesUseCase
import com.hmc.listacompracompose.features.items.domain.GetItemListUseCase
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.lists.domain.AddItemToListUseCase
import com.hmc.listacompracompose.features.lists.domain.DeleteShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.domain.ObserveListItemsUseCase
import com.hmc.listacompracompose.features.lists.domain.ObserveShoppingListsUseCase
import com.hmc.listacompracompose.features.lists.domain.RemoveItemFromListUseCase
import com.hmc.listacompracompose.features.lists.domain.RenameShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.RestoreItemToListUseCase
import com.hmc.listacompracompose.features.lists.domain.SetItemAmountUseCase
import com.hmc.listacompracompose.features.lists.domain.SetItemCheckedUseCase
import com.hmc.listacompracompose.features.lists.domain.SetPaidPriceUseCase
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

enum class ListMode { COMPRA, EDICION }

data class ListDetailUiState(
    val listName: String = "",
    val items: List<ListItemModel> = emptyList(),
    val picker: PickerData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val undo: UndoPrompt? = null,
    val mode: ListMode = ListMode.COMPRA
) {
    val total: Double get() = items.sumOf { (it.effectivePrice ?: 0.0) * it.amount }
    val itemsWithoutPrice: Int get() = items.count { it.effectivePrice == null }
    val checkedCount: Int get() = items.count { it.isChecked }
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
    private val setPaidPriceUseCase: SetPaidPriceUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val renameShoppingListUseCase: RenameShoppingListUseCase,
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase
) : ViewModel() {

    private val _mode = MutableStateFlow(ListMode.COMPRA)
    private val _picker = MutableStateFlow<PickerData?>(null)
    private val _error = MutableStateFlow<String?>(null)
    private val _undo = MutableStateFlow<UndoPrompt?>(null)
    private var lastRemoved: ListItemModel? = null

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ListDetailUiState> = combine(
        observeListsUseCase(),
        observeListItemsUseCase(listId).catch { e ->
            Log.e(TAG, "Error al observar los productos de la lista", e)
            _error.value = "No se pudo cargar la lista"
            emit(emptyList())
        },
        _picker,
        _mode,
        _error,
        _undo
    ) { flows: Array<Any?> ->
        val lists =
            flows[0] as List<com.hmc.listacompracompose.features.lists.domain.ShoppingListModel>
        val items = flows[1] as List<ListItemModel>
        val picker = flows[2] as PickerData?
        val mode = flows[3] as ListMode
        val error = flows[4] as String?
        val undo = flows[5] as UndoPrompt?

        val idsInList = items.map { it.itemId }.toSet()
        ListDetailUiState(
            listName = lists.firstOrNull { it.id == listId }?.name.orEmpty(),
            items = items,
            picker = picker?.copy(items = picker.items.filter { it.id !in idsInList }),
            isLoading = false,
            error = error,
            undo = undo,
            mode = mode
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
            restoreItemToListUseCase(listId, it.itemId, it.amount, it.isChecked, it.paidPrice)
        }
        lastRemoved = null
    }

    // Guarda el precio pagado y registra el histórico del producto
    fun setPaidPrice(itemId: Int, price: Double) = launchSafely("No se pudo guardar el precio") {
        setPaidPriceUseCase(listId, itemId, price)
    }

    fun clearPaidPrice(itemId: Int) = launchSafely("No se pudo quitar el precio") {
        setPaidPriceUseCase.clear(listId, itemId)
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

    fun setMode(mode: ListMode) {
        _mode.value = mode
    }

    fun renameList(name: String) = launchSafely("No se pudo renombrar la lista") {
        renameShoppingListUseCase(listId, name)
    }

    fun deleteList(onSuccess: () -> Unit = {}) = launchSafely("No se pudo borrar la lista") {
        deleteShoppingListUseCase(listId)
        onSuccess()
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