package com.hmc.listacompracompose.features.items.data

import android.content.Context
import android.util.Log
import com.hmc.listacompracompose.features.items.data.db.ItemDbLocalDataSource
import com.hmc.listacompracompose.features.items.data.mock.mockItemList
import com.hmc.listacompracompose.features.items.data.remote.ItemApiRemoteDataSource
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.items.domain.ItemRepository
import org.koin.core.annotation.Single

@Single
class ItemDataRepository(
    private val localDataSource: ItemDbLocalDataSource,
    private val remoteDataSource: ItemApiRemoteDataSource,
    context: Context
) : ItemRepository {

    private val prefs = context.getSharedPreferences("items_prefs", Context.MODE_PRIVATE)

    override suspend fun getItemList(): List<ItemModel> {
        if (!prefs.getBoolean(KEY_INITIAL_LOADED, false)) {

            // --- OPCIÓN MOCK ---
            localDataSource.saveItemList(mockItemList)
            Log.d(TAG, "Mock guardado en la BD local")

            // --- OPCIÓN API ---
            /*
            val remoteList = remoteDataSource.getItemList()
            localDataSource.saveItemList(remoteList)
            Log.d(TAG, "Datos remotos guardados con éxito")
            */

            prefs.edit().putBoolean(KEY_INITIAL_LOADED, true).apply()
        }
        return localDataSource.getItemList()
    }

    override suspend fun insertItem(item: ItemModel): Boolean {
        return try {
            localDataSource.insertItem(item)
            Log.d(TAG, "Ítem insertado con éxito en la BD local: ${item.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar el ítem: ${item.name}", e)
            false
        }
    }

    override suspend fun deleteItem(item: ItemModel) {
        localDataSource.deleteItem(item)
    }

    override suspend fun updateItem(item: ItemModel) {
        localDataSource.updateItem(item)
    }

    private companion object {
        const val TAG = "ListaCompra"
        const val KEY_INITIAL_LOADED = "mock_loaded"
    }
}