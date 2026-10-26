package com.hmc.listacompracompose.features.pricehistory.data

import com.hmc.listacompracompose.app.util.dayRange
import com.hmc.listacompracompose.features.data.local.db.dao.PriceHistoryDao
import com.hmc.listacompracompose.features.data.local.db.entities.PriceHistoryEntity
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class PriceHistoryDbLocalDataSource(
    private val priceHistoryDao: PriceHistoryDao
) : PriceHistoryLocalDataSource {

    override fun observeByItem(itemId: Int): Flow<List<PriceHistoryModel>> =
        priceHistoryDao.observeByItem(itemId).map { list -> list.map { it.toModel() } }

    override fun observeLastPrices(): Flow<Map<Int, Double>> =
        priceHistoryDao.observeLastPrices().map { list -> list.associate { it.itemId to it.price } }

    // Un precio por producto y día: si ya había uno ese día, se reemplaza
    override suspend fun recordPrice(itemId: Int, price: Double, recordedAt: Long) {
        val (dayStart, dayEnd) = dayRange(recordedAt)
        priceHistoryDao.record(
            entry = PriceHistoryEntity(itemId = itemId, price = price, recordedAt = recordedAt),
            dayStart = dayStart,
            dayEnd = dayEnd
        )
    }
}