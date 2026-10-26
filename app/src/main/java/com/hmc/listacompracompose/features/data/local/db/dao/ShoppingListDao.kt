package com.hmc.listacompracompose.features.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hmc.listacompracompose.features.data.local.db.entities.ListItemRow
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListItemEntity
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_CATEGORY
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_ITEM_NAME
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SHOPPING_LIST
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SHOPPING_LIST_ITEM
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM $TABLE_SHOPPING_LIST WHERE supermarketId = :supermarketId ORDER BY id")
    fun observeLists(supermarketId: Int): Flow<List<ShoppingListEntity>>

    @Insert
    suspend fun insertList(list: ShoppingListEntity): Long

    @Query("DELETE FROM $TABLE_SHOPPING_LIST WHERE id = :listId")
    suspend fun deleteList(listId: Int)

    @Query(
        """
    SELECT i.id AS itemId, i.name, i.price, li.paidPrice AS paidPrice,
           li.amount, li.isChecked,
           c.id AS categoryId, c.name AS categoryName
    FROM $TABLE_SHOPPING_LIST_ITEM li
    INNER JOIN $TABLE_ITEM_NAME i ON i.id = li.itemId
    LEFT JOIN $TABLE_CATEGORY c ON c.id = i.categoryId
    WHERE li.listId = :listId
    ORDER BY c.position IS NULL, c.position, i.name
    """
    )
    fun observeListItems(listId: Int): Flow<List<ListItemRow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItemToList(entry: ShoppingListItemEntity)

    @Query("DELETE FROM $TABLE_SHOPPING_LIST_ITEM WHERE listId = :listId AND itemId = :itemId")
    suspend fun removeItemFromList(listId: Int, itemId: Int)

    @Query("UPDATE $TABLE_SHOPPING_LIST_ITEM SET isChecked = :checked WHERE listId = :listId AND itemId = :itemId")
    suspend fun setChecked(listId: Int, itemId: Int, checked: Boolean)

    @Query("UPDATE $TABLE_SHOPPING_LIST_ITEM SET amount = :amount WHERE listId = :listId AND itemId = :itemId")
    suspend fun setAmount(listId: Int, itemId: Int, amount: Int)

    @Query("UPDATE $TABLE_SHOPPING_LIST_ITEM SET paidPrice = :price WHERE listId = :listId AND itemId = :itemId")
    suspend fun setPaidPrice(listId: Int, itemId: Int, price: Double?)

    @Query("UPDATE $TABLE_SHOPPING_LIST SET name = :name WHERE id = :listId")
    suspend fun renameList(listId: Int, name: String)
}