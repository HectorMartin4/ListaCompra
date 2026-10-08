package com.hmc.listacompracompose.features.items.domain

interface ItemRepository {

    suspend fun getItemList(): List<ItemModel>

    suspend fun insertItem(item: ItemModel): Boolean

    suspend fun deleteItem(item: ItemModel)
}