package com.hmc.listacompracompose.features.items.data.remote

import android.util.Log
import com.hmc.listacompracompose.features.items.domain.ItemModel
import org.koin.core.annotation.Single

@Single
class ItemApiRemoteDataSource(private val apiService: ApiService) {

    suspend fun getItemList(): List<ItemModel> {

        Log.d("Keloke", "Antes del getItemList")
        val response = apiService.getItemList()

        Log.d("Keloke", "getItemList: ${response.body()}")

        return if (response.isSuccessful) {
            response.body()!!.map {
                it.toModel()
            }

        } else {
            emptyList()
        }
    }

    suspend fun deleteItem(id: Int): Boolean {
        Log.d("Keloke", "Antes de deleteItem con ID: $id")
        return try {
            val response = apiService.deleteItem(id)
            Log.d("Keloke", "deleteItem response code: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Keloke", "Error al borrar el ítem: ${e.message}", e)
            false
        }
    }
}