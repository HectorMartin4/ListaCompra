package com.hmc.listacompracompose.features.items.data.remote

import com.hmc.listacompracompose.features.items.domain.ItemModel


fun ItemModel.toApiModel() =
    ItemApiModel(
        this.id,
        this.name,
        this.price,
    )

fun ItemApiModel.toModel() =
    ItemModel(
        this.id,
        this.name,
        this.price,
        amount = 0,
        check = false,
    )