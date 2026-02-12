package com.hmc.listacompracompose.features.items.domain

data class ItemModel(
    val id: Int,
    val name: String,
    val amount: Int,
    val check: Boolean
)
