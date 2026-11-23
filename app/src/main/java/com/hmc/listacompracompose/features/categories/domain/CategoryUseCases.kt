package com.hmc.listacompracompose.features.categories.domain

import com.hmc.listacompracompose.app.util.normalizeName
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<CategoryModel>> = repository.observeCategories()

    // Categorías de un supermercado concreto (al añadir productos a una lista de ese supermercado)
    operator fun invoke(supermarketId: Int): Flow<List<CategoryModel>> =
        repository.observeCategories(supermarketId)
}

@Single
class GetCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(supermarketId: Int): List<CategoryModel> =
        repository.getCategories(supermarketId)
}

@Single
class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String) = repository.createCategory(name.normalizeName())
}

@Single
class RenameCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int, name: String) =
        repository.renameCategory(id, name.normalizeName())
}

@Single
class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteCategory(id)
}

@Single
class ReorderCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(orderedIds: List<Int>) = repository.reorderCategories(orderedIds)
}