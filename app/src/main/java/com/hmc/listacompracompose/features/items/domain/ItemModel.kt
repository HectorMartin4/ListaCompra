package com.hmc.listacompracompose.features.items.domain

data class ItemModel(
    val id: Int = 0,
    val name: String,
    val price: Double? = null,
    val categoryId: Int? = null
)
