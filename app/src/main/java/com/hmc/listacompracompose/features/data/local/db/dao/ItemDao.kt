package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_ITEM_NAME

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItems(items: List<ItemEntity>)

    @Query("SELECT * FROM ${TABLE_ITEM_NAME}")
    suspend fun getAllItems(): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)
}