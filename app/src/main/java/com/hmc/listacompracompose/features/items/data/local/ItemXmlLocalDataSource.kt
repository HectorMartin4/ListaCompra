package com.hmc.listacompracompose.features.items.data.local

import android.content.Context
import com.hmc.listacompracompose.app.Serializer
import com.hmc.listacompracompose.features.items.domain.ItemModel
import org.koin.core.annotation.Single

@Single
class ItemXmlLocalDataSource(private val context: Context, private val serializer: Serializer) {

    private val sharedPreferences = context.getSharedPreferences("ItemList", Context.MODE_PRIVATE)

    fun saveItem(item: ItemModel) {
        sharedPreferences.edit().apply{
            putString(item.id, serializer.toJson(item, ItemModel::class.java))
        }.apply()
    }

    fun saveItemList(itemList: List<ItemModel>){
        itemList.map {
            saveItem(it)
        }
    }

    fun getItemList(): List<ItemModel>{
        return sharedPreferences.all.map {
            serializer.fromJson(it.value as String, ItemModel::class.java)
        }
    }
}