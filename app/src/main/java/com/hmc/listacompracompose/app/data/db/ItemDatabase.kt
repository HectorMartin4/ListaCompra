package com.hmc.listacompracompose.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hmc.listacompracompose.features.items.data.local.db.ItemDao
import com.hmc.listacompracompose.features.items.data.local.db.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}