package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hmc.listacompracompose.features.data.local.db.entities.LastPriceRow
import com.hmc.listacompracompose.features.data.local.db.entities.PriceHistoryEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_PRICE_HISTORY
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Query("SELECT * FROM $TABLE_PRICE_HISTORY WHERE itemId = :itemId ORDER BY recordedAt DESC")
    fun observeByItem(itemId: Int): Flow<List<PriceHistoryEntity>>

    @Query(
        """
        SELECT itemId AS itemId, price AS price, recordedAt AS recordedAt
        FROM $TABLE_PRICE_HISTORY
        WHERE (itemId, recordedAt) IN (
            SELECT itemId, MAX(recordedAt)
            FROM $TABLE_PRICE_HISTORY
            GROUP BY itemId
        )
        """
    )
    fun observeLastPrices(): Flow<List<LastPriceRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PriceHistoryEntity): Long

    @Query("DELETE FROM $TABLE_PRICE_HISTORY WHERE itemId = :itemId AND recordedAt >= :dayStart AND recordedAt < :dayEnd")
    suspend fun deleteDay(itemId: Int, dayStart: Long, dayEnd: Long)

    // Un precio por producto y día: corregir el precio del mismo día lo reemplaza
    @Transaction
    suspend fun record(entry: PriceHistoryEntity, dayStart: Long, dayEnd: Long) {
        deleteDay(entry.itemId, dayStart, dayEnd)
        insert(entry)
    }
}