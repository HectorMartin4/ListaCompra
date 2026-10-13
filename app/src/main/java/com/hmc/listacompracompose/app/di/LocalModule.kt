package com.hmc.listacompracompose.app.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import com.hmc.listacompracompose.app.data.db.ItemDatabase
import com.hmc.listacompracompose.app.data.db.MIGRATION_3_4
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class LocalModule {

    @Single
    fun provideRoom(context: Context): ItemDatabase {
        val builder = Room.databaseBuilder(context, ItemDatabase::class.java, "shopping-list-db")
            .addMigrations(MIGRATION_3_4)

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            builder.fallbackToDestructiveMigration(true)
        }
        return builder.build()
    }
}
