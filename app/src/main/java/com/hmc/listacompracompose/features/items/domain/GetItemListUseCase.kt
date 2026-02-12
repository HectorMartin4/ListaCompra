package com.hmc.listacompracompose.features.items.domain

import org.koin.core.annotation.Single

@Single
class GetItemListUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(): List<ItemModel> = itemRepository.getItemList()
}