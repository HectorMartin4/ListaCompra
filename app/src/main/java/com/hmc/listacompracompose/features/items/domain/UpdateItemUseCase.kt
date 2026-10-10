package com.hmc.listacompracompose.features.items.domain

import org.koin.core.annotation.Single

@Single
class UpdateItemUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(item: ItemModel): Unit = itemRepository.updateItem(item)
}