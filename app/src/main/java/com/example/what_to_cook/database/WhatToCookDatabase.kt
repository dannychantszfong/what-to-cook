package com.example.what_to_cook.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.what_to_cook.database.dao.ApiKeyDao
import com.example.what_to_cook.database.dao.IngredientDao
import com.example.what_to_cook.database.dao.RecipeDao
import com.example.what_to_cook.database.entities.ApiKeyEntity
import com.example.what_to_cook.database.entities.IngredientEntity
import com.example.what_to_cook.database.entities.RecipeEntity

@Database(
    entities = [IngredientEntity::class, RecipeEntity::class, ApiKeyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class WhatToCookDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        @Volatile
        private var INSTANCE: WhatToCookDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add type column to ingredients table
                database.execSQL("ALTER TABLE ingredients ADD COLUMN type TEXT NOT NULL DEFAULT 'REGULAR'")
                
                // Create API keys table
                database.execSQL("""
                    CREATE TABLE api_keys (
                        id TEXT PRIMARY KEY NOT NULL,
                        aiModel TEXT NOT NULL,
                        apiKey TEXT NOT NULL,
                        customModelName TEXT NOT NULL DEFAULT '',
                        customApiEndpoint TEXT NOT NULL DEFAULT '',
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): WhatToCookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WhatToCookDatabase::class.java,
                    "what_to_cook_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 