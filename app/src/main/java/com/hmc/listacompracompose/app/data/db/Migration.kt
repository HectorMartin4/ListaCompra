package com.hmc.listacompracompose.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_CATEGORY
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_ITEM_NAME


val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `$TABLE_CATEGORY` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL)"
        )
        db.execSQL(
            "ALTER TABLE `$TABLE_ITEM_NAME` ADD COLUMN `categoryId` INTEGER " +
                    "REFERENCES `$TABLE_CATEGORY`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_${TABLE_ITEM_NAME}_categoryId` " +
                    "ON `$TABLE_ITEM_NAME` (`categoryId`)"
        )
    }
}