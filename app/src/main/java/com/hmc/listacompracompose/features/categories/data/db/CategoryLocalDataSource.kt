package com.hmc.listacompracompose.features.categories.data.db

import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.data.local.db.dao.CategoryDao
import com.hmc.listacompracompose.features.supermarkets.data.ActiveSupermarketProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    // Categorías de un supermercado concreto (usado al añadir productos a una lista
    // de ese supermercado, independientemente del supermercado activo)
    fun observeCategories(supermarketId: Int): Flow<List<CategoryModel>> =
        dao.observeCategories(supermarketId).map { list -> list.map { it.toModel() } }

    suspend fun getCategories(supermarketId: Int): List<CategoryModel> =
        observeCategories(supermarketId).first()

    suspend fun createCategory(name: String) =
        dao.insertAtEnd(name, activeSupermarket.current())

    suspend fun renameCategory(id: Int, name: String) = dao.rename(id, name)

    suspend fun deleteCategory(id: Int) = dao.delete(id)

    suspend fun reorderCategories(orderedIds: List<Int>) = dao.reorder(orderedIds)
}