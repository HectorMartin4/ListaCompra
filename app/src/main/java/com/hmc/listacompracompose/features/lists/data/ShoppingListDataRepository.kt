package com.hmc.listacompracompose.features.lists.data

import com.hmc.listacompracompose.features.lists.data.local.ShoppingListDbLocalDataSource
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
import com.hmc.listacompracompose.features.lists.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ShoppingListDataRepository(
    private val localDataSource: ShoppingListDbLocalDataSource
) : ShoppingListRepository {

    override fun observeLists(): Flow<List<ShoppingListModel>> =
        localDataSource.observeLists()

    override suspend fun createList(name: String) = localDataSource.createList(name)

    override suspend fun deleteList(listId: Int) = localDataSource.deleteList(listId)

    override fun observeListItems(listId: Int): Flow<List<ListItemModel>> =
        localDataSource.observeListItems(listId)

    override suspend fun addItemToList(listId: Int, itemId: Int) =
        localDataSource.addItemToList(listId, itemId)

    override suspend fun removeItemFromList(listId: Int, itemId: Int) =
        localDataSource.removeItemFromList(listId, itemId)

    override suspend fun setChecked(listId: Int, itemId: Int, checked: Boolean) =
        localDataSource.setChecked(listId, itemId, checked)

    override suspend fun setAmount(listId: Int, itemId: Int, amount: Int) =
        localDataSource.setAmount(listId, itemId, amount)
}