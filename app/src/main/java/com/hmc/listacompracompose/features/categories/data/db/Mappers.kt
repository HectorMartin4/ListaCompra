package com.hmc.listacompracompose.features.categories.data.db

import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.data.local.db.entities.CategoryEntity

fun CategoryEntity.toModel() = CategoryModel(
    id = id,
    name = name,
    position = position
)