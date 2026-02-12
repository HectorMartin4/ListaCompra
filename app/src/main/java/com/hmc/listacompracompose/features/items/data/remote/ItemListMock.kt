package com.hmc.listacompracompose.features.items.data.remote

import com.hmc.listacompracompose.features.items.domain.ItemModel

class ItemListMock {

    val itemsMock = listOf(
        ItemModel(id = "1", name = "Manzanas", amount = 10, check = false),
        ItemModel(id = "2", name = "Pan", amount = 2, check = true),
        ItemModel(id = "3", name = "Leche", amount = 1, check = false),
        ItemModel(id = "4", name = "Huevos", amount = 12, check = true),
        ItemModel(id = "5", name = "Cereales", amount = 3, check = false)
    )

    fun getItemList(): List<ItemModel> =
        itemsMock
}