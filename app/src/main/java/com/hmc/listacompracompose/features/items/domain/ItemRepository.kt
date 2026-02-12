package com.hmc.listacompracompose.features.items.domain

interface ItemRepository {

    suspend fun getItemList(): List<ItemModel>
}