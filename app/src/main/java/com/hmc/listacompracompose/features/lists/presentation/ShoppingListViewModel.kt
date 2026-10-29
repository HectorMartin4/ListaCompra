package com.hmc.listacompracompose.features.lists.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.lists.domain.CreateShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.DeleteShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.ObserveAllShoppingListsUseCase
import com.hmc.listacompracompose.features.lists.domain.ObserveListCompletionUseCase
import com.hmc.listacompracompose.features.lists.domain.RenameShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveActiveSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveSupermarketsUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ShoppingListsUiState(
    val lists: List<ShoppingListModel> = emptyList(),
    val supermarkets: List<SupermarketModel> = emptyList(),
    val activeSupermarketId: Int? = null,
    val listCompletion: Map<Int, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@KoinViewModel
class ShoppingListsViewModel(
    observeAllListsUseCase: ObserveAllShoppingListsUseCase,
    observeListCompletionUseCase: ObserveListCompletionUseCase,
    observeSupermarketsUseCase: ObserveSupermarketsUseCase,
    observeActiveSupermarketUseCase: ObserveActiveSupermarketUseCase,
    private val createListUseCase: CreateShoppingListUseCase,
    private val deleteListUseCase: DeleteShoppingListUseCase,
    private val renameListUseCase: RenameShoppingListUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShoppingListsUiState> = combine(
        observeAllListsUseCase().catch { e ->
            Log.e(TAG, "Error al observar las listas", e)
            _error.value = "No se pudieron cargar las listas"
            emit(emptyList())
        },
        observeSupermarketsUseCase().catch { e ->
            Log.e(TAG, "Error al observar los supermercados", e)
            _error.value = "No se pudieron cargar los supermercados"
            emit(emptyList())
        },
        observeActiveSupermarketUseCase(),
        observeListCompletionUseCase().catch { e ->
            Log.e(TAG, "Error al observar completitud", e)
            emit(emptyMap())
        },
        _error
    ) { lists, supermarkets, active, completion, error ->
        ShoppingListsUiState(
            lists = lists,
            supermarkets = supermarkets,
            activeSupermarketId = active?.id,
            listCompletion = completion,
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListsUiState()
    )

    fun createList(name: String, supermarketId: Int) = launchSafely("No se pudo crear la lista") {
        createListUseCase(name, supermarketId)
    }

    fun deleteList(listId: Int) = launchSafely("No se pudo borrar la lista") {
        deleteListUseCase(listId)
    }

    fun renameList(id: Int, name: String) = launchSafely("No se pudo renombrar la lista") {
        renameListUseCase(id, name)
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