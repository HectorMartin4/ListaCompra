package com.hmc.listacompracompose.features.lists.data.local

import com.hmc.listacompracompose.features.data.local.db.entities.AllListRow
import com.hmc.listacompracompose.features.data.local.db.entities.ListItemRow
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListEntity
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel

fun ShoppingListEntity.toModel() = ShoppingListModel(
    id = id,
    name = name
)

fun AllListRow.toModel() = ShoppingListModel(
    id = listId,
    name = listName,
    supermarketId = supermarketId,
    supermarketName = supermarketName
)

fun ListItemRow.toModel() = ListItemModel(
    itemId = itemId,
    name = name,
    price = price,
    paidPrice = paidPrice,
    amount = amount,
    isChecked = isChecked,
    categoryId = categoryId,
    categoryName = categoryName
)