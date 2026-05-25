package com.hmc.listacompracompose.features.items.data.local.db

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
}