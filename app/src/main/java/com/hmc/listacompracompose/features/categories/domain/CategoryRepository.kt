package com.hmc.listacompracompose.features.categories.domain

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<CategoryModel>>
    fun observeCategories(supermarketId: Int): Flow<List<CategoryModel>>
    suspend fun getCategories(supermarketId: Int): List<CategoryModel>
    suspend fun createCategory(name: String)
    suspend fun renameCategory(id: Int, name: String)
    suspend fun deleteCategory(id: Int)
    suspend fun reorderCategories(orderedIds: List<Int>)
}