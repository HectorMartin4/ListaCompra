package com.hmc.listacompracompose.features.lists.domain

import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {

    fun observeLists(): Flow<List<ShoppingListModel>>

    suspend fun createList(name: String)
    suspend fun deleteList(listId: Int)

    fun observeListItems(listId: Int): Flow<List<ListItemModel>>

    suspend fun addItemToList(listId: Int, itemId: Int)
    suspend fun removeItemFromList(listId: Int, itemId: Int)
    suspend fun setChecked(listId: Int, itemId: Int, checked: Boolean)
    suspend fun setAmount(listId: Int, itemId: Int, amount: Int)
}