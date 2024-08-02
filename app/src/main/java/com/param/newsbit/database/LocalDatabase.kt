package com.param.newsbit.database

import android.content.Context
import androidx.room.*
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.entity.News

@Database(entities = [News::class], version = 3, exportSchema = false)
@TypeConverters(Converter::class)
abstract class LocalDatabase : RoomDatabase() {

    public abstract fun newsDao(): NewsDao

    companion object {

        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "local_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }

        }

    }

}