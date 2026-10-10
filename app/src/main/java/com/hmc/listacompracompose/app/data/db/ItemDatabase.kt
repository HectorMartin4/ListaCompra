package com.hmc.listacompracompose.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hmc.listacompracompose.features.data.local.db.dao.CategoryDao
import com.hmc.listacompracompose.features.data.local.db.dao.ItemDao
import com.hmc.listacompracompose.features.data.local.db.dao.ShoppingListDao
import com.hmc.listacompracompose.features.data.local.db.entities.CategoryEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ItemEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListEntity
import com.hmc.listacompracompose.features.data.local.db.entities.ShoppingListItemEntity

@Database(
    entities = [
        ItemEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        CategoryEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun categoryDao(): CategoryDao
}