package com.hmc.listacompracompose.features.items.domain

import org.koin.core.annotation.Single

@Single
class InsertItemUseCase (private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Boolean = itemRepository.insertItem(item)
}