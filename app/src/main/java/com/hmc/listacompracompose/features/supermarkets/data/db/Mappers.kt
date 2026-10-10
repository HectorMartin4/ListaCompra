package com.hmc.listacompracompose.features.supermarkets.data.db

import com.hmc.listacompracompose.features.data.local.db.entities.SupermarketEntity
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel

fun SupermarketEntity.toModel() = SupermarketModel(
    id = id,
    name = name
)