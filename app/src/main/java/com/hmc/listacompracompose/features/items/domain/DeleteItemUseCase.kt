package com.hmc.listacompracompose.features.items.domain

import org.koin.core.annotation.Single

@Single
class DeleteItemUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Unit = itemRepository.deleteItem(item)
}