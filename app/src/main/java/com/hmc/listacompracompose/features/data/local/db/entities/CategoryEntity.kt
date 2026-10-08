package com.hmc.listacompracompose.features.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_CATEGORY = "category"

@Entity(tableName = TABLE_CATEGORY)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: Int
)
