package com.brenda.fitnesscheck.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Type Converters for Room
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}

// Updated Room Database with UserProfile support
@Database(
    entities = [
        ChallengeEntity::class,
        UserProfileEntity::class  // Added UserProfile entity
    ],
    version = 2,  // Incremented version from 1 to 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun challengeDao(): ChallengeDao
    abstract fun userProfileDao(): UserProfileDao  // Added UserProfile DAO

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Migration from version 1 to 2 - adds user_profile table
         * This preserves your existing challenge data while adding profile support
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the user_profile table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `stepGoal` INTEGER NOT NULL,
                        `waterGoal` REAL NOT NULL,
                        `sleepGoal` REAL NOT NULL,
                        `profilePictureIndex` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Insert default profile for existing users
                val currentTime = System.currentTimeMillis()
                database.execSQL("""
                    INSERT OR REPLACE INTO `user_profile` 
                    (`id`, `name`, `stepGoal`, `waterGoal`, `sleepGoal`, `profilePictureIndex`, `createdAt`, `updatedAt`) 
                    VALUES (1, 'Brenda', 10000, 2.0, 8.0, 0, $currentTime, $currentTime)
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    .addMigrations(MIGRATION_1_2)  // Add migration for production
                    .fallbackToDestructiveMigration()  // For development - remove in production
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * For testing purposes - provides in-memory database
         */
        fun getInMemoryDatabase(context: Context): FitnessDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                FitnessDatabase::class.java
            )
                .allowMainThreadQueries() // Only for testing
                .build()
        }

        /**
         * Clear the database instance (useful for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}