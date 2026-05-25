package com.hmc.listacompracompose.features.items.data

import com.hmc.listacompracompose.features.items.data.local.db.ItemDbLocalDataSource
import com.hmc.listacompracompose.features.items.data.remote.ItemApiRemoteDataSource
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.items.domain.ItemRepository
import org.koin.core.annotation.Single

@Single
class ItemDataRepository(
    private val localDataSource: ItemDbLocalDataSource,
    private val remoteDataSource: ItemApiRemoteDataSource
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