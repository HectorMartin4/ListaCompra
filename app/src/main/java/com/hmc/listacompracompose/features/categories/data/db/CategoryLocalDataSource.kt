package com.hmc.listacompracompose.features.categories.data.db

import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.data.local.db.dao.CategoryDao
import com.hmc.listacompracompose.features.supermarkets.data.ActiveSupermarketProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class CategoryLocalDataSource(
    private val dao: CategoryDao,
    private val activeSupermarket: ActiveSupermarketProvider
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCategories(): Flow<List<CategoryModel>> =
        activeSupermarket.activeId
            .flatMapLatest { supermarketId -> dao.observeCategories(supermarketId) }
            .map { list -> list.map { it.toModel() } }

    suspend fun createCategory(name: String) =
        dao.insertAtEnd(name, activeSupermarket.current())

    suspend fun renameCategory(id: Int, name: String) = dao.rename(id, name)

    suspend fun deleteCategory(id: Int) = dao.delete(id)

    suspend fun reorderCategories(orderedIds: List<Int>) = dao.reorder(orderedIds)
}