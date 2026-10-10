package com.hmc.listacompracompose.app.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hmc.listacompracompose.app.data.db.ItemDatabase
import com.hmc.listacompracompose.app.data.db.MIGRATION_3_4
import com.hmc.listacompracompose.app.data.db.MIGRATION_4_5
import com.hmc.listacompracompose.features.data.local.db.entities.DEFAULT_SUPERMARKET_ID
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SUPERMARKET
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class LocalModule {

    @Single
    fun provideRoom(context: Context): ItemDatabase {
        val builder = Room.databaseBuilder(context, ItemDatabase::class.java, "shopping-list-db")
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "INSERT INTO `$TABLE_SUPERMARKET` (`id`, `name`) " +
                                "VALUES ($DEFAULT_SUPERMARKET_ID, 'Mi supermercado')"
                    )
                }
            })

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            builder.fallbackToDestructiveMigration(true)
        }
        return builder.build()
    }
}
