package com.hmc.listacompracompose.features.pricehistory.data

import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import kotlinx.coroutines.flow.Flow

interface PriceHistoryLocalDataSource {
    fun observeByItem(itemId: Int): Flow<List<PriceHistoryModel>>

    // Mapa: itemId -> último precio registrado
    fun observeLastPrices(): Flow<Map<Int, Double>>
    suspend fun recordPrice(itemId: Int, price: Double, recordedAt: Long)
}