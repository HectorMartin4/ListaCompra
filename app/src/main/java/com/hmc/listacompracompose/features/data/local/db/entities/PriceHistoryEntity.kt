package com.hmc.listacompracompose.features.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_PRICE_HISTORY = "price_history"

@Entity(
    tableName = TABLE_PRICE_HISTORY,
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val price: Double,
    val recordedAt: Long   //  System.currentTimeMillis()
)

// Precio más reciente registrado para un producto (para mostrar en el catálogo)
data class LastPriceRow(
    val itemId: Int,
    val price: Double,
    val recordedAt: Long
)