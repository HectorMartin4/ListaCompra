package com.hmc.listacompracompose.features.items.domain

import com.hmc.listacompracompose.app.util.normalizeName
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveItemListUseCase(private val itemRepository: ItemRepository) {
    operator fun invoke(): Flow<List<ItemModel>> = itemRepository.observeItemList()
}

@Single
class GetItemListUseCase(private val itemRepository: ItemRepository) {
    suspend operator fun invoke(): List<ItemModel> = itemRepository.getItemList()

    // Catálogo de un supermercado concreto (al añadir a una lista de ese supermercado)
    suspend operator fun invoke(supermarketId: Int): List<ItemModel> =
        itemRepository.getItemList(supermarketId)
}

@Single
class InsertItemUseCase (private val itemRepository: ItemRepository) {
    suspend operator fun invoke(item: ItemModel): Boolean =
        itemRepository.insertItem(item.copy(name = item.name.normalizeName()))
}

@Single
class DeleteItemUseCase(private val itemRepository: ItemRepository) {
    suspend operator fun invoke(item: ItemModel): Unit = itemRepository.deleteItem(item)
}

@Single
class UpdateItemUseCase(private val itemRepository: ItemRepository) {
    suspend operator fun invoke(item: ItemModel) =
        itemRepository.updateItem(item.copy(name = item.name.normalizeName()))
}