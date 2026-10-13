package com.hmc.listacompracompose.features.items.domain

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeItemList(): Flow<List<ItemModel>>
    suspend fun getItemList(): List<ItemModel>
    suspend fun insertItem(item: ItemModel): Boolean
    suspend fun updateItem(item: ItemModel)
    suspend fun deleteItem(item: ItemModel)
}