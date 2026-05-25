package com.hmc.listacompracompose.features.items.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun saveItems(items: List<ItemEntity>)

    @Query("SELECT * FROM $TABLE_ITEM_NAME")
    fun getAllItems(): List<ItemEntity>

    @Delete
    suspend fun deleteItem(item: ItemEntity)
}

