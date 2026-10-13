package com.hmc.listacompracompose.features.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_SUPERMARKET = "supermarket"
const val DEFAULT_SUPERMARKET_ID = 1

@Entity(tableName = TABLE_SUPERMARKET)
data class SupermarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)