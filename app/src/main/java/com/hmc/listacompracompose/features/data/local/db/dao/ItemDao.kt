package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_ITEM_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM $TABLE_ITEM_NAME WHERE supermarketId = :supermarketId ORDER BY name")
    fun observeItems(supermarketId: Int): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveItems(items: List<ItemEntity>)

    @Insert
    suspend fun insertItem(item: ItemEntity)

    @Query("UPDATE $TABLE_ITEM_NAME SET name = :name, price = :price, categoryId = :categoryId WHERE id = :id")
    suspend fun updateItem(id: Int, name: String, price: Double?, categoryId: Int?)

    @Query("DELETE FROM $TABLE_ITEM_NAME WHERE id = :id")
    suspend fun deleteItem(id: Int)
}