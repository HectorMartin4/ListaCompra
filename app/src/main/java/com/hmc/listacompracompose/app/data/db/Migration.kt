package com.hmc.listacompracompose.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hmc.listacompracompose.features.data.local.db.entities.DEFAULT_SUPERMARKET_ID
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_CATEGORY
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_ITEM_NAME
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SHOPPING_LIST
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SHOPPING_LIST_ITEM
import com.hmc.listacompracompose.features.data.local.db.entities.TABLE_SUPERMARKET


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

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val items = TABLE_ITEM_NAME
        val category = TABLE_CATEGORY
        val list = TABLE_SHOPPING_LIST
        val listItem = TABLE_SHOPPING_LIST_ITEM
        val sm = TABLE_SUPERMARKET

        // 1. Supermercado por defecto
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `$sm` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
        )
        db.execSQL("INSERT INTO `$sm` (`id`, `name`) VALUES ($DEFAULT_SUPERMARKET_ID, 'Mi supermercado')")

        // 2. Copias de seguridad (tablas simples, sin claves foráneas)
        db.execSQL("CREATE TABLE `_bak_category` AS SELECT * FROM `$category`")
        db.execSQL("CREATE TABLE `_bak_list` AS SELECT * FROM `$list`")
        db.execSQL("CREATE TABLE `_bak_items` AS SELECT * FROM `$items`")
        db.execSQL("CREATE TABLE `_bak_list_item` AS SELECT * FROM `$listItem`")

        // 3. Borrar las tablas antiguas, de hijas a padres, para que no salten cascadas
        db.execSQL("DROP TABLE `$listItem`")
        db.execSQL("DROP TABLE `$items`")
        db.execSQL("DROP TABLE `$list`")
        db.execSQL("DROP TABLE `$category`")

        // 4. Crear las tablas nuevas, de padres a hijas
        db.execSQL(
            "CREATE TABLE `$category` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "`supermarketId` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`supermarketId`) REFERENCES `$sm`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX `index_${category}_supermarketId` ON `$category` (`supermarketId`)")

        db.execSQL(
            "CREATE TABLE `$list` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`supermarketId` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`supermarketId`) REFERENCES `$sm`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX `index_${list}_supermarketId` ON `$list` (`supermarketId`)")

        db.execSQL(
            "CREATE TABLE `$items` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`price` REAL NOT NULL, " +
                    "`categoryId` INTEGER, " +
                    "`supermarketId` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`categoryId`) REFERENCES `$category`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, " +
                    "FOREIGN KEY(`supermarketId`) REFERENCES `$sm`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX `index_${items}_categoryId` ON `$items` (`categoryId`)")
        db.execSQL("CREATE INDEX `index_${items}_supermarketId` ON `$items` (`supermarketId`)")

        db.execSQL(
            "CREATE TABLE `$listItem` (" +
                    "`listId` INTEGER NOT NULL, " +
                    "`itemId` INTEGER NOT NULL, " +
                    "`amount` INTEGER NOT NULL, " +
                    "`isChecked` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`listId`, `itemId`), " +
                    "FOREIGN KEY(`listId`) REFERENCES `$list`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`itemId`) REFERENCES `$items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX `index_${listItem}_itemId` ON `$listItem` (`itemId`)")

        // 5. Restaurar los datos, de padres a hijas, asignándolos al supermercado por defecto
        db.execSQL(
            "INSERT INTO `$category` (`id`, `name`, `position`, `supermarketId`) " +
                    "SELECT `id`, `name`, `position`, $DEFAULT_SUPERMARKET_ID FROM `_bak_category`"
        )
        db.execSQL(
            "INSERT INTO `$list` (`id`, `name`, `supermarketId`) " +
                    "SELECT `id`, `name`, $DEFAULT_SUPERMARKET_ID FROM `_bak_list`"
        )
        db.execSQL(
            "INSERT INTO `$items` (`id`, `name`, `price`, `categoryId`, `supermarketId`) " +
                    "SELECT `id`, `name`, `price`, `categoryId`, $DEFAULT_SUPERMARKET_ID FROM `_bak_items`"
        )
        db.execSQL(
            "INSERT INTO `$listItem` (`listId`, `itemId`, `amount`, `isChecked`) " +
                    "SELECT `listId`, `itemId`, `amount`, `isChecked` FROM `_bak_list_item`"
        )

        // 6. Borrar las copias
        db.execSQL("DROP TABLE `_bak_category`")
        db.execSQL("DROP TABLE `_bak_list`")
        db.execSQL("DROP TABLE `_bak_items`")
        db.execSQL("DROP TABLE `_bak_list_item`")
    }
}