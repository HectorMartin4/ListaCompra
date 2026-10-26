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
import com.hmc.listacompracompose.features.pricehistory.domain.ObserveLastPricesUseCase
import com.hmc.listacompracompose.features.pricehistory.domain.ObservePriceHistoryUseCase
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import com.hmc.listacompracompose.features.pricehistory.domain.RecordPriceUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import java.util.Date

data class ItemListUiState(
    val itemList: List<ItemModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val detail: ItemDetailState? = null,
    val lastPrices: Map<Int, Double> = emptyMap()
)

data class ItemDetailState(
    val item: ItemModel,
    val history: List<PriceHistoryModel> = emptyList()
)

@KoinViewModel
class ItemListViewModel(
    observeItemListUseCase: ObserveItemListUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val insertItemUseCase: InsertItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val observePriceHistoryUseCase: ObservePriceHistoryUseCase,
    private val observeLastPricesUseCase: ObserveLastPricesUseCase,
    private val recordPriceUseCase: RecordPriceUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _detailItemId = MutableStateFlow<Int?>(null)

    // Últimos precios de todos los productos (para mostrar en el catálogo)
    private val lastPricesFlow: Flow<Map<Int, Double>> = observeLastPricesUseCase()

    // El histórico del producto abierto en el bottom sheet; null cuando no hay sheet
    @OptIn(ExperimentalCoroutinesApi::class)
    private val priceHistoryFlow: Flow<List<PriceHistoryModel>> =
        _detailItemId.flatMapLatest { itemId ->
            if (itemId == null) flowOf(emptyList())
            else observePriceHistoryUseCase(itemId)
        }.catch { e ->
            Log.e(TAG, "Error al observar el histórico de precios", e)
            emit(emptyList())
        }

    @Suppress("UNCHECKED_CAST")
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
        _error,
        _detailItemId,
        priceHistoryFlow,
        lastPricesFlow
    ) { flows: Array<Any?> ->
        val items = flows[0] as List<ItemModel>
        val categories = flows[1] as List<CategoryModel>
        val error = flows[2] as String?
        val detailItemId = flows[3] as Int?
        val history = flows[4] as List<PriceHistoryModel>
        val lastPrices = flows[5] as Map<Int, Double>

        val detail = detailItemId?.let { id ->
            items.firstOrNull { it.id == id }?.let { item ->
                ItemDetailState(item = item, history = history)
            }
        }
        ItemListUiState(
            itemList = items,
            categories = categories,
            isLoading = false,
            error = error,
            detail = detail,
            lastPrices = lastPrices
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

    fun openDetail(item: ItemModel) {
        _detailItemId.value = item.id
    }

    fun closeDetail() {
        _detailItemId.value = null
    }

    // Fecha por defecto: hoy. El usuario puede elegir otra para registrar precios pasados
    fun addHistoryPrice(price: Double, date: Date = Date()) =
        launchSafely("No se pudo guardar el precio") {
            val itemId = _detailItemId.value ?: return@launchSafely
            recordPriceUseCase(itemId, price, date.time)
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