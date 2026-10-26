package com.hmc.listacompracompose.features.pricehistory.data

import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class PriceHistoryDataRepository(
    private val localDataSource: PriceHistoryLocalDataSource
) : PriceHistoryRepository {

    override fun observeByItem(itemId: Int): Flow<List<PriceHistoryModel>> =
        localDataSource.observeByItem(itemId)

    override fun observeLastPrices(): Flow<Map<Int, Double>> =
        localDataSource.observeLastPrices()

    override suspend fun recordPrice(itemId: Int, price: Double, recordedAt: Long) =
        localDataSource.recordPrice(itemId, price, recordedAt)
}