package com.hmc.listacompracompose.features.categories.domain

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<CategoryModel>> = repository.observeCategories()
}

@Single
class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String) = repository.createCategory(name.trim())
}

@Single
class RenameCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int, name: String) = repository.renameCategory(id, name.trim())
}

@Single
class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteCategory(id)
}

@Single
class ReorderCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(orderedIds: List<Int>) = repository.reorderCategories(orderedIds)
}