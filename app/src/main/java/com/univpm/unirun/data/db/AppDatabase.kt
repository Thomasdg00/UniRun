package com.univpm.unirun.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.univpm.unirun.data.db.dao.ActivityDao
import com.univpm.unirun.data.db.dao.GearDao
import com.univpm.unirun.data.db.dao.UserDao
import com.univpm.unirun.data.db.dao.WaterLogDao

@Database(
    entities = [UserEntity::class, ActivityEntity::class, GearEntity::class, WaterLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun activityDao(): ActivityDao
    abstract fun gearDao(): GearDao
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unirun_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
