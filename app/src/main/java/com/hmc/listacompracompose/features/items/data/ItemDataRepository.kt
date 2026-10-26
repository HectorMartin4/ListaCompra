package com.hmc.listacompracompose.features.items.data

import android.content.Context
import android.util.Log
import com.hmc.listacompracompose.features.items.data.db.ItemDbLocalDataSource
import com.hmc.listacompracompose.features.items.data.remote.ItemApiRemoteDataSource
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.items.domain.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Single

@Single
class ItemDataRepository(
    private val localDataSource: ItemDbLocalDataSource,
    private val remoteDataSource: ItemApiRemoteDataSource,
    context: Context
) : ItemRepository {

    private val prefs = context.getSharedPreferences("items_prefs", Context.MODE_PRIVATE)

    override fun observeItemList(): Flow<List<ItemModel>> =
        localDataSource.observeItems().onStart { loadInitialDataIfNeeded() }

    override suspend fun getItemList(): List<ItemModel> = observeItemList().first()

    private suspend fun loadInitialDataIfNeeded() {
        if (prefs.getBoolean(KEY_INITIAL_LOADED, false)) return

        // Catálogo vacío al instalar: el usuario crea sus propios productos
        // (los mocks de ItemMock.kt quedaron para desarrollo puntual)

        // --- OPCIÓN API ---
        /*
        val remoteList = remoteDataSource.getItemList()
        localDataSource.saveItemList(remoteList, DEFAULT_SUPERMARKET_ID)
        Log.d(TAG, "Datos remotos guardados con éxito")
        */

        prefs.edit().putBoolean(KEY_INITIAL_LOADED, true).apply()
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

    override suspend fun updateItem(item: ItemModel) = localDataSource.updateItem(item)

    override suspend fun deleteItem(item: ItemModel) = localDataSource.deleteItem(item)

    private companion object {
        const val TAG = "ListaCompra"
        const val KEY_INITIAL_LOADED = "initial_data_loaded"
    }
}