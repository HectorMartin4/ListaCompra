package com.hmc.listacompracompose.features.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_CATEGORY = "category"

@Entity(
    tableName = TABLE_CATEGORY,
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supermarketId")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: Int,
    val supermarketId: Int
)
