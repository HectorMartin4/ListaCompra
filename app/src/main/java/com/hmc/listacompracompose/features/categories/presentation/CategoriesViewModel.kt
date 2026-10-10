package com.hmc.listacompracompose.features.categories.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.categories.domain.CreateCategoryUseCase
import com.hmc.listacompracompose.features.categories.domain.DeleteCategoryUseCase
import com.hmc.listacompracompose.features.categories.domain.ObserveCategoriesUseCase
import com.hmc.listacompracompose.features.categories.domain.RenameCategoryUseCase
import com.hmc.listacompracompose.features.categories.domain.ReorderCategoriesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class CategoriesUiState(
    val categories: List<CategoryModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@KoinViewModel
class CategoriesViewModel(
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val renameCategoryUseCase: RenameCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val reorderCategoriesUseCase: ReorderCategoriesUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        observeCategoriesUseCase().catch { e ->
            Log.e(TAG, "Error al observar las categorías", e)
            _error.value = "No se pudieron cargar las categorías"
            emit(emptyList())
        },
        _error
    ) { categories, error ->
        CategoriesUiState(categories = categories, isLoading = false, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState()
    )

    fun createCategory(name: String) = launchSafely("No se pudo crear la categoría") {
        createCategoryUseCase(name)
    }

    fun renameCategory(id: Int, name: String) = launchSafely("No se pudo renombrar la categoría") {
        renameCategoryUseCase(id, name)
    }

    fun deleteCategory(id: Int) = launchSafely("No se pudo borrar la categoría") {
        deleteCategoryUseCase(id)
    }

    fun reorder(orderedIds: List<Int>) = launchSafely("No se pudo guardar el orden") {
        reorderCategoriesUseCase(orderedIds)
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