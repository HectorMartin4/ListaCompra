package com.hmc.listacompracompose.features.items.domain

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveItemListUseCase(private val itemRepository: ItemRepository) {
    operator fun invoke(): Flow<List<ItemModel>> = itemRepository.observeItemList()
}

@Single
class GetItemListUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(): List<ItemModel> = itemRepository.getItemList()
}

@Single
class InsertItemUseCase (private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Boolean = itemRepository.insertItem(item)
}

@Single
class DeleteItemUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Unit = itemRepository.deleteItem(item)
}

@Single
class UpdateItemUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Unit = itemRepository.updateItem(item)
}