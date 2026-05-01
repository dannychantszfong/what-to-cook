package com.example.what_to_cook.repository

import com.example.what_to_cook.data.AppError
import com.example.what_to_cook.data.Ingredient
import com.example.what_to_cook.data.IngredientType
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.dao.IngredientDao
import com.example.what_to_cook.database.entities.toEntity
import com.example.what_to_cook.database.entities.toIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class IngredientRepository(
    private val ingredientDao: IngredientDao
) {
    fun getAllIngredients(): Flow<Result<List<Ingredient>>> = flow {
        try {
            ingredientDao.getAllIngredients().collect { entities ->
                emit(Result.Success(entities.map { it.toIngredient() }))
            }
        } catch (e: Exception) {
            emit(Result.Error<List<Ingredient>>(AppError.DatabaseError("Failed to load ingredients: ${e.message}"), e.message ?: "Unknown error"))
        }
    }

    fun getIngredientsByType(type: IngredientType): Flow<Result<List<Ingredient>>> = flow {
        try {
            ingredientDao.getIngredientsByType(type.name).collect { entities ->
                emit(Result.Success(entities.map { it.toIngredient() }))
            }
        } catch (e: Exception) {
            emit(Result.Error<List<Ingredient>>(AppError.DatabaseError("Failed to load ${type.name.lowercase()} ingredients: ${e.message}"), e.message ?: "Unknown error"))
        }
    }

    suspend fun getIngredientById(id: String): Result<Ingredient?> {
        return try {
            val ingredient = ingredientDao.getIngredientById(id)?.toIngredient()
            Result.Success(ingredient)
        } catch (e: Exception) {
            Result.Error<Ingredient?>(AppError.DatabaseError("Failed to get ingredient: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun insertIngredient(ingredient: Ingredient): Result<Unit> {
        return try {
            ingredientDao.insertIngredient(ingredient.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to add ingredient: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun insertIngredients(ingredients: List<Ingredient>): Result<Unit> {
        return try {
            ingredientDao.insertIngredients(ingredients.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to add ingredients: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun updateIngredient(ingredient: Ingredient): Result<Unit> {
        return try {
            ingredientDao.updateIngredient(ingredient.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to update ingredient: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteIngredient(ingredient: Ingredient): Result<Unit> {
        return try {
            ingredientDao.deleteIngredient(ingredient.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to delete ingredient: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteIngredientById(id: String): Result<Unit> {
        return try {
            ingredientDao.deleteIngredientById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to delete ingredient: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteIngredientsByType(type: IngredientType): Result<Unit> {
        return try {
            ingredientDao.deleteIngredientsByType(type.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to delete ${type.name.lowercase()} ingredients: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteAllIngredients(): Result<Unit> {
        return try {
            ingredientDao.deleteAllIngredients()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error<Unit>(AppError.DatabaseError("Failed to delete all ingredients: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun getIngredientCount(): Result<Int> {
        return try {
            val count = ingredientDao.getIngredientCount()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error<Int>(AppError.DatabaseError("Failed to get ingredient count: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun getIngredientCountByType(type: IngredientType): Result<Int> {
        return try {
            val count = ingredientDao.getIngredientCountByType(type.name)
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error<Int>(AppError.DatabaseError("Failed to get ${type.name.lowercase()} ingredient count: ${e.message}"), e.message ?: "Unknown error")
        }
    }
} 