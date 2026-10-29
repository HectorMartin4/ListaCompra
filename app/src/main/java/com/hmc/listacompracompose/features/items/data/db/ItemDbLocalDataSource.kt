package com.hmc.listacompracompose.features.items.data.db

import com.hmc.listacompracompose.features.data.local.db.dao.ItemDao
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.supermarkets.data.ActiveSupermarketProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ItemDbLocalDataSource(
    private val itemDao: ItemDao,
    private val activeSupermarket: ActiveSupermarketProvider
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeItems(): Flow<List<ItemModel>> =
        activeSupermarket.activeId
            .flatMapLatest { supermarketId -> itemDao.observeItems(supermarketId) }
            .map { list -> list.map { it.toModel() } }

    // Catálogo de un supermercado concreto (usado al añadir productos a una lista
    // de ese supermercado, independientemente del supermercado activo)
    fun observeItems(supermarketId: Int): Flow<List<ItemModel>> =
        itemDao.observeItems(supermarketId).map { list -> list.map { it.toModel() } }

    suspend fun getItemList(supermarketId: Int): List<ItemModel> =
        observeItems(supermarketId).first()

    // Para la carga inicial: se indica el supermercado explícitamente
    suspend fun saveItemList(items: List<ItemModel>, supermarketId: Int) {
        itemDao.saveItems(items.map { it.toEntity(supermarketId) })
    }

    suspend fun insertItem(item: ItemModel) {
        itemDao.insertItem(item.toEntity(activeSupermarket.current()))
    }

    suspend fun updateItem(item: ItemModel) {
        itemDao.updateItem(item.id, item.name, item.price, item.categoryId)
    }

    suspend fun deleteItem(item: ItemModel) {
        itemDao.deleteItem(item.id)
    }
}