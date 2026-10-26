package com.hmc.listacompracompose.features.pricehistory.domain

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObservePriceHistoryUseCase(private val repository: PriceHistoryRepository) {
    operator fun invoke(itemId: Int): Flow<List<PriceHistoryModel>> =
        repository.observeByItem(itemId)
}

@Single
class ObserveLastPricesUseCase(private val repository: PriceHistoryRepository) {
    operator fun invoke(): Flow<Map<Int, Double>> =
        repository.observeLastPrices()
}

@Single
class RecordPriceUseCase(private val repository: PriceHistoryRepository) {
    suspend operator fun invoke(itemId: Int, price: Double, recordedAt: Long) =
        repository.recordPrice(itemId, price, recordedAt)
}