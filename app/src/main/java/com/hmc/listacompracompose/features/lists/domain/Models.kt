package com.hmc.listacompracompose.features.lists.domain

data class ShoppingListModel(
    val id: Int = 0,
    val name: String
)

data class ListItemModel(
    val itemId: Int,
    val name: String,
    val price: Double,
    val amount: Int,
    val isChecked: Boolean,
    val categoryId: Int? = null,
    val categoryName: String? = null
)