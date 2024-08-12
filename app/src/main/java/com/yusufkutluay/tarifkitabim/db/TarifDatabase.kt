package com.yusufkutluay.tarifkitabim.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yusufkutluay.tarifkitabim.model.Tarif

@Database(entities = [Tarif::class], version = 1)
abstract class TarifDatabase : RoomDatabase() {
    abstract fun tarifDao(): TarifDao
}
