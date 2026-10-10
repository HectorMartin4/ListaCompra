package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hmc.listacompracompose.features.data.local.db.entities.CategoryEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_CATEGORY
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM $TABLE_CATEGORY WHERE supermarketId = :supermarketId ORDER BY position")
    fun observeCategories(supermarketId: Int): Flow<List<CategoryEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM $TABLE_CATEGORY WHERE supermarketId = :supermarketId")
    suspend fun nextPosition(supermarketId: Int): Int

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Query("UPDATE $TABLE_CATEGORY SET name = :name WHERE id = :id")
    suspend fun rename(id: Int, name: String)

    @Query("DELETE FROM $TABLE_CATEGORY WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("UPDATE $TABLE_CATEGORY SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Int, position: Int)

    @Transaction
    suspend fun insertAtEnd(name: String, supermarketId: Int) {
        insert(
            CategoryEntity(
                name = name,
                position = nextPosition(supermarketId),
                supermarketId = supermarketId
            )
        )
    }

    @Transaction
    suspend fun reorder(orderedIds: List<Int>) {
        orderedIds.forEachIndexed { index, id -> setPosition(id, index) }
    }
}

