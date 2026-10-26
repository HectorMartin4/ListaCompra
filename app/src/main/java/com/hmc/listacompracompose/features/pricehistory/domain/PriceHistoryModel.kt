package com.hmc.listacompracompose.features.pricehistory.domain

import java.util.Date

data class PriceHistoryModel(
    val id: Int = 0,
    val itemId: Int,
    val price: Double,
    val recordedAt: Date
)