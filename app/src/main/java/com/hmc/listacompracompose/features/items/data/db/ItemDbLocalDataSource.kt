package com.hmc.listacompracompose.features.items.data.db

import com.hmc.listacompracompose.features.data.local.db.dao.ItemDao
import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.items.domain.ItemModel
import org.koin.core.annotation.Single

@Single
class ItemDbLocalDataSource(private val itemDao: ItemDao) {

    suspend fun saveItemList(items: List<ItemModel>) {
        val itemList: List<ItemEntity> = items.map {
            it.toEntity()
        }
        itemDao.saveItems(itemList)
    }

    suspend fun getItemList(): List<ItemModel> =
        itemDao.getAllItems().map {
            it.toModel()
        }

    suspend fun insertItem(item: ItemModel) {
        itemDao.insertItem(item.toEntity())
    }

    suspend fun deleteItem(item: ItemModel) {
        itemDao.deleteItem(item.toEntity())
    }

    suspend fun updateItem(item: ItemModel) {
        itemDao.updateItem(item.toEntity())
    }
}