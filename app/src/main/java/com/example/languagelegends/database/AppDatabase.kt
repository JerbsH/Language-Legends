package com.example.languagelegends.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


/**
 * This is the main database class for the application.
 * You define all entities and DAOs associated with the database.
 */
@Database(entities = [UserProfile::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

}

object DatabaseProvider {
    private var database: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "language_legends_database"
            ).fallbackToDestructiveMigration().build()
        }
        return database!!
    }
}