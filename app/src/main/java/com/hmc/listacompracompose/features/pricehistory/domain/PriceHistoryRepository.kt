package com.hmc.listacompracompose.features.pricehistory.domain

import kotlinx.coroutines.flow.Flow

interface PriceHistoryRepository {

    fun observeByItem(itemId: Int): Flow<List<PriceHistoryModel>>

    fun observeLastPrices(): Flow<Map<Int, Double>>

    suspend fun recordPrice(itemId: Int, price: Double, recordedAt: Long)

}