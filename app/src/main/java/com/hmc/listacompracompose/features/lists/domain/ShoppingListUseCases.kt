package com.hmc.listacompracompose.features.lists.domain

import com.hmc.listacompracompose.app.util.normalizeName
import com.hmc.listacompracompose.features.pricehistory.domain.PriceHistoryRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

// Todas las listas de todos los supermercados, con el nombre del supermercado
@Single
class ObserveAllShoppingListsUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(): Flow<List<ShoppingListModel>> = repository.observeAllLists()
}

@Single
class CreateShoppingListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(name: String, supermarketId: Int) =
        repository.createList(name.normalizeName(), supermarketId)
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
    suspend operator fun invoke(
        listId: Int,
        itemId: Int,
        amount: Int,
        isChecked: Boolean,
        paidPrice: Double? = null
    ) = repository.restoreItemToList(listId, itemId, amount, isChecked, paidPrice)
}

// Guarda el precio pagado en la lista y lo añade al histórico del producto con la fecha actual
@Single
class SetPaidPriceUseCase(
    private val repository: ShoppingListRepository,
    private val priceHistoryRepository: PriceHistoryRepository
) {
    suspend operator fun invoke(listId: Int, itemId: Int, price: Double) {
        repository.setPaidPrice(listId, itemId, price)
        priceHistoryRepository.recordPrice(itemId, price, System.currentTimeMillis())
    }

    // Quitar el precio pagado no borra el histórico ya registrado
    suspend fun clear(listId: Int, itemId: Int) = repository.setPaidPrice(listId, itemId, null)
}

// Estado de completitud de todas las listas (para mostrar en la vista principal)
@Single
class ObserveListCompletionUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(): Flow<Map<Int, Boolean>> = repository.observeListCompletion()
}