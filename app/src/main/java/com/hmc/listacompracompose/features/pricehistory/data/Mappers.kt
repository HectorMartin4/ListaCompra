package com.hmc.listacompracompose.features.pricehistory.data

import com.hmc.listacompracompose.features.data.local.db.entities.PriceHistoryEntity
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryModel
import java.util.Date

fun PriceHistoryEntity.toModel(): PriceHistoryModel = PriceHistoryModel(
    id = id,
    itemId = itemId,
    price = price,
    recordedAt = Date(recordedAt)
)

fun PriceHistoryModel.toEntity(): PriceHistoryEntity = PriceHistoryEntity(
    id = id,
    itemId = itemId,
    price = price,
    recordedAt = recordedAt.time
)