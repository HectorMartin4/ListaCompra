package com.hmc.listacompracompose.features.lists.domain

import com.hmc.listacompracompose.app.util.normalizeName
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveShoppingListsUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(): Flow<List<ShoppingListModel>> = repository.observeLists()
}

@Single
class CreateShoppingListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(name: String) = repository.createList(name.normalizeName())
}

@Single
class DeleteShoppingListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int) = repository.deleteList(listId)
}

@Single
class ObserveListItemsUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(listId: Int): Flow<List<ListItemModel>> = repository.observeListItems(listId)
}

@Single
class AddItemToListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int, itemId: Int) = repository.addItemToList(listId, itemId)
}

@Single
class RemoveItemFromListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int, itemId: Int) = repository.removeItemFromList(listId, itemId)
}

@Single
class SetItemCheckedUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int, itemId: Int, checked: Boolean) =
        repository.setChecked(listId, itemId, checked)
}

@Single
class SetItemAmountUseCase(private val repository: ShoppingListRepository) {
    // La cantidad mínima es 1; para quitar un producto se usa RemoveItemFromListUseCase
    suspend operator fun invoke(listId: Int, itemId: Int, amount: Int) =
        repository.setAmount(listId, itemId, amount.coerceAtLeast(1))
}

@Single
class RenameShoppingListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int, name: String) =
        repository.renameList(listId, name.normalizeName())
}

@Single
class RestoreItemToListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: Int, itemId: Int, amount: Int, isChecked: Boolean) =
        repository.restoreItemToList(listId, itemId, amount, isChecked)
}