package com.hmc.listacompracompose.features.lists.domain

import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {

    // Todas las listas de todos los supermercados, con el nombre del supermercado
    fun observeAllLists(): Flow<List<ShoppingListModel>>

    suspend fun createList(name: String, supermarketId: Int)
    suspend fun deleteList(listId: Int)

    fun observeListItems(listId: Int): Flow<List<ListItemModel>>

    suspend fun addItemToList(listId: Int, itemId: Int)
    suspend fun removeItemFromList(listId: Int, itemId: Int)
    suspend fun setChecked(listId: Int, itemId: Int, checked: Boolean)
    suspend fun setAmount(listId: Int, itemId: Int, amount: Int)
    suspend fun renameList(listId: Int, name: String)
    suspend fun restoreItemToList(
        listId: Int,
        itemId: Int,
        amount: Int,
        isChecked: Boolean,
        paidPrice: Double?
    )

    suspend fun setPaidPrice(listId: Int, itemId: Int, price: Double?)

    // Completitud de listas
    fun observeListCompletion(): Flow<Map<Int, Boolean>>
}