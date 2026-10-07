package com.hmc.listacompracompose.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hmc.listacompracompose.features.data.local.db.dao.ItemDao
import com.hmc.listacompracompose.features.data.local.db.dao.ShoppingListDao
import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListItemEntity

@Database(
    entities = [
        ItemEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class
               ],
    version = 3,
    exportSchema = false
)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun shoppingListDao(): ShoppingListDao
}