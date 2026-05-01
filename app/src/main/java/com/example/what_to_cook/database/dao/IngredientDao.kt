package com.example.what_to_cook.database.dao

import androidx.room.*
import com.example.what_to_cook.database.entities.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY createdAt DESC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE type = :type ORDER BY createdAt DESC")
    fun getIngredientsByType(type: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: String): IngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: String)

    @Query("DELETE FROM ingredients WHERE type = :type")
    suspend fun deleteIngredientsByType(type: String)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()

    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun getIngredientCount(): Int

    @Query("SELECT COUNT(*) FROM ingredients WHERE type = :type")
    suspend fun getIngredientCountByType(type: String): Int
} 