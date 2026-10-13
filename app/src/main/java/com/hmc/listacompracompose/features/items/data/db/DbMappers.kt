package com.hmc.listacompracompose.features.items.data.db

import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.items.domain.ItemModel

fun ItemModel.toEntity(supermarketId: Int) = ItemEntity(
    id = id,
    name = name,
    price = price,
    categoryId = categoryId,
    supermarketId = supermarketId
)

fun ItemEntity.toModel() = ItemModel(
    id = id,
    name = name,
    price = price,
    categoryId = categoryId
)