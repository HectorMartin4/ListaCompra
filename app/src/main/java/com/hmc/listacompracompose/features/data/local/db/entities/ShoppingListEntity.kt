package com.hmc.listacompracompose.features.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_SHOPPING_LIST = "shopping_list"
const val TABLE_SHOPPING_LIST_ITEM = "shopping_list_item"

@Entity(
    tableName = TABLE_SHOPPING_LIST,
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
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val supermarketId: Int
)

@Entity(
    tableName = TABLE_SHOPPING_LIST_ITEM,
    primaryKeys = ["listId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class ShoppingListItemEntity(
    val listId: Int,
    val itemId: Int,
    val amount: Int = 1,
    val isChecked: Boolean = false
)

data class ListItemRow(
    val itemId: Int,
    val name: String,
    val price: Double,
    val amount: Int,
    val isChecked: Boolean,
    val categoryId: Int?,
    val categoryName: String?
)
