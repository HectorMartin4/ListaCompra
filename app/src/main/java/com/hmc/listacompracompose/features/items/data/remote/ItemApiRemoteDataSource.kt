package com.hmc.listacompracompose.features.items.data.remote

import com.hmc.listacompracompose.features.items.domain.ItemModel
import org.koin.core.annotation.Single

@Single
class ItemApiRemoteDataSource(private val apiService: ApiService) {

    suspend fun getItemList(): List<ItemModel> {
        val response = apiService.getItemList()

        return if (response.isSuccessful) {
            response.body()!!.map {
                it.toModel()
            }

        } else {
            emptyList()
        }
    }
}