package com.hmc.listacompracompose.features.lists.data.local

import com.hmc.listacompracompose.features.data.local.db.dao.ShoppingListDao
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListItemEntity
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ShoppingListDbLocalDataSource(private val dao: ShoppingListDao) {
    fun observeLists(): Flow<List<ShoppingListModel>> =
        dao.observeLists().map { lists -> lists.map { it.toModel() } }

    suspend fun createList(name: String) {
        dao.insertList(ShoppingListEntity(name = name))
    }

    suspend fun deleteList(listId: Int) = dao.deleteList(listId)

    fun observeListItems(listId: Int): Flow<List<ListItemModel>> =
        dao.observeListItems(listId).map { rows -> rows.map { it.toModel() } }

    suspend fun addItemToList(listId: Int, itemId: Int) {
        dao.addItemToList(ShoppingListItemEntity(listId = listId, itemId = itemId))
    }

    suspend fun removeItemFromList(listId: Int, itemId: Int) =
        dao.removeItemFromList(listId, itemId)

    suspend fun setChecked(listId: Int, itemId: Int, checked: Boolean) =
        dao.setChecked(listId, itemId, checked)

    suspend fun setAmount(listId: Int, itemId: Int, amount: Int) =
        dao.setAmount(listId, itemId, amount)
}
