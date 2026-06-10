package com.hmc.listacompracompose.features.items.data

import android.util.Log
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
        try {
            Log.d("Keloke", "Antes de localDataSource")

            var itemList = localDataSource.getItemList()

            Log.d("Keloke", "Después de localDataSource")

            return if (itemList.isEmpty()) {
                Log.d("Keloke", "log2")

                itemList = remoteDataSource.getItemList()
                localDataSource.saveItemList(itemList)

                itemList
            } else {
                Log.d("Keloke", "log3")
                itemList
            }
        } catch (e: Exception) {
            Log.e("Keloke", "Error en getItemList", e)
            throw e
        }
    }
}