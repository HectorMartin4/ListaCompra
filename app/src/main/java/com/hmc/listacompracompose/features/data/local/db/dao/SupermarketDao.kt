package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hmc.listacompracompose.features.data.local.db.entities.SupermarketEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SUPERMARKET
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketDao {

    @Query("SELECT * FROM $TABLE_SUPERMARKET ORDER BY id")
    fun observeSupermarkets(): Flow<List<SupermarketEntity>>

    @Query("SELECT COUNT(*) FROM $TABLE_SUPERMARKET")
    suspend fun count(): Int

    @Insert
    suspend fun insert(supermarket: SupermarketEntity): Long

    @Query("UPDATE $TABLE_SUPERMARKET SET name = :name WHERE id = :id")
    suspend fun rename(id: Int, name: String)

    @Query("DELETE FROM $TABLE_SUPERMARKET WHERE id = :id")
    suspend fun delete(id: Int)

    @Transaction
    suspend fun deleteIfNotLast(id: Int): Boolean {
        if (count() <= 1) return false
        delete(id)
        return true
    }
}