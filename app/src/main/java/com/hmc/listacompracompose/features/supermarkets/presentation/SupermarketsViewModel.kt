package com.hmc.listacompracompose.features.supermarkets.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.supermarkets.domain.CreateSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.DeleteSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveActiveSupermarketUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.ObserveSupermarketsUseCase
import com.hmc.listacompracompose.features.supermarkets.domain.RenameSupermarketUseCase
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

data class SupermarketsUiState(
    val supermarkets: List<SupermarketModel> = emptyList(),
    val activeId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@KoinViewModel
class SupermarketsViewModel(
    observeSupermarketsUseCase: ObserveSupermarketsUseCase,
    observeActiveSupermarketUseCase: ObserveActiveSupermarketUseCase,
    private val createSupermarketUseCase: CreateSupermarketUseCase,
    private val renameSupermarketUseCase: RenameSupermarketUseCase,
    private val deleteSupermarketUseCase: DeleteSupermarketUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SupermarketsUiState> = combine(
        observeSupermarketsUseCase().catch { e ->
            Log.e(TAG, "Error al observar los supermercados", e)
            _error.value = "No se pudieron cargar los supermercados"
            emit(emptyList())
        },
        observeActiveSupermarketUseCase(),
        _error
    ) { supermarkets, active, error ->
        SupermarketsUiState(
            supermarkets = supermarkets,
            activeId = active?.id,
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupermarketsUiState()
    )

    fun createSupermarket(name: String) = launchSafely("No se pudo crear el supermercado") {
        createSupermarketUseCase(name)
    }

    fun renameSupermarket(id: Int, name: String) =
        launchSafely("No se pudo renombrar el supermercado") {
            renameSupermarketUseCase(id, name)
        }

    fun deleteSupermarket(id: Int) = launchSafely("No se pudo borrar el supermercado") {
        if (!deleteSupermarketUseCase(id)) {
            _error.value = "Tiene que quedar al menos un supermercado"
        }
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