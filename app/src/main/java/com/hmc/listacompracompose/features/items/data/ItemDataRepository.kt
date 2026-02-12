package com.hmc.listacompracompose.features.items.data

import com.hmc.listacompracompose.features.items.data.local.ItemXmlLocalDataSource
import com.hmc.listacompracompose.features.items.data.remote.ItemListMock
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.items.domain.ItemRepository
import org.koin.core.annotation.Single

@Single
class ItemDataRepository(
    private val localDataSource: ItemXmlLocalDataSource,
    private val remoteDataSource: ItemListMock
): ItemRepository {

    override suspend fun getItemList(): List<ItemModel> {
        var itemList = localDataSource.getItemList()

        return if (localDataSource.getItemList().isEmpty()){
            itemList = remoteDataSource.getItemList()

            localDataSource.saveItemList(itemList)
            itemList
        } else {
            itemList
        }
    }
}