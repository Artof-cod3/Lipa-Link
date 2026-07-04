package com.yourteam.debttracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Debt::class], version = 1, exportSchema = false)
abstract class DebtDatabase : RoomDatabase() {

    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var INSTANCE: DebtDatabase? = null

        fun getDatabase(context: Context): DebtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DebtDatabase::class.java,
                    "debt_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
