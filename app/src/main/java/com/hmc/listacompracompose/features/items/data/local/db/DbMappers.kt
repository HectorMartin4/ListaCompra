package com.hmc.listacompracompose.features.items.data.local.db

import com.hmc.listacompracompose.features.items.domain.ItemModel

fun ItemModel.toEntity() =
    ItemEntity(
        this.id,
        this.name,
        this.amount,
        this.check,
        this.price
    )

fun ItemEntity.toModel() =
    ItemModel(
        this.id,
        this.name,
        this.price,
        this.amount,
        this.isChecked
    )