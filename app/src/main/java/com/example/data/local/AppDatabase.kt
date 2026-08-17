package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BetDao
import com.example.data.local.entity.BetItemEntity
import com.example.data.local.entity.DailySettlementEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.SlipEntity

@Database(
    entities = [
        SlipEntity::class,
        BetItemEntity::class,
        DailySettlementEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun betDao(): BetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "twod_dealer_ledger.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
