package com.hmc.listacompracompose.features.lists.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.lists.domain.CreateShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.DeleteShoppingListUseCase
import com.hmc.listacompracompose.features.lists.domain.ObserveShoppingListsUseCase
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
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
    val isLoading: Boolean = true,
    val error: String? = null
)

@KoinViewModel
class ShoppingListsViewModel(
    observeListsUseCase: ObserveShoppingListsUseCase,
    private val createListUseCase: CreateShoppingListUseCase,
    private val deleteListUseCase: DeleteShoppingListUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShoppingListsUiState> = combine(
        observeListsUseCase().catch { e ->
            Log.e(TAG, "Error al observar las listas", e)
            _error.value = "No se pudieron cargar las listas"
            emit(emptyList())
        },
        _error
    ) { lists, error ->
        ShoppingListsUiState(lists = lists, isLoading = false, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListsUiState()
    )

    fun createList(name: String) = launchSafely("No se pudo crear la lista") {
        createListUseCase(name)
    }

    fun deleteList(listId: Int) = launchSafely("No se pudo borrar la lista") {
        deleteListUseCase(listId)
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