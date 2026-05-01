package com.example.what_to_cook.repository

import com.example.what_to_cook.data.AppError
import com.example.what_to_cook.data.Recipe
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.dao.RecipeDao
import com.example.what_to_cook.database.entities.RecipeEntity
import com.example.what_to_cook.database.entities.toEntity
import com.example.what_to_cook.database.entities.toRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class RecipeRepository(
    private val recipeDao: RecipeDao
) {
    fun getAllRecipes(): Flow<Result<List<RecipeEntity>>> = flow {
        try {
            recipeDao.getAllRecipes().collect { entities ->
                emit(Result.Success(entities))
            }
        } catch (e: Exception) {
            emit(Result.Error(AppError.DatabaseError("Failed to load recipes: ${e.message}"), e.message ?: "Unknown error"))
        }
    }

    fun getFavoriteRecipes(): Flow<Result<List<Recipe>>> = flow {
        try {
            recipeDao.getFavoriteRecipes().collect { entities ->
                emit(Result.Success(entities.map { it.toRecipe() }))
            }
        } catch (e: Exception) {
            emit(Result.Error(AppError.DatabaseError("Failed to load favorite recipes: ${e.message}"), e.message ?: "Unknown error"))
        }
    }

    suspend fun getRecipeById(id: String): Result<Recipe?> {
        return try {
            val recipe = recipeDao.getRecipeById(id)?.toRecipe()
            Result.Success(recipe)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to get recipe: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun insertRecipe(recipe: Recipe, sourceIngredients: String, isFavorite: Boolean = false): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val entity = recipe.toEntity(id, sourceIngredients, isFavorite)
            recipeDao.insertRecipe(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to save recipe: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun insertRecipes(recipes: List<Recipe>, sourceIngredients: String): Result<Unit> {
        return try {
            val entities = recipes.map { recipe ->
                // Use existing ID if available, otherwise generate new one
                val id = if (recipe.id.isNotBlank()) recipe.id else UUID.randomUUID().toString()
                recipe.toEntity(id, sourceIngredients, recipe.isFavorite)
            }
            recipeDao.insertRecipes(entities)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to save recipes: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun updateRecipe(recipe: Recipe, id: String, sourceIngredients: String, isFavorite: Boolean): Result<Unit> {
        return try {
            val entity = recipe.toEntity(id, sourceIngredients, isFavorite)
            recipeDao.updateRecipe(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to update recipe: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean): Result<Unit> {
        return try {
            recipeDao.updateFavoriteStatus(id, isFavorite)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to update favorite status: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteRecipe(recipe: Recipe, id: String, sourceIngredients: String, isFavorite: Boolean): Result<Unit> {
        return try {
            val entity = recipe.toEntity(id, sourceIngredients, isFavorite)
            recipeDao.deleteRecipe(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to delete recipe: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteRecipeById(id: String): Result<Unit> {
        return try {
            recipeDao.deleteRecipeById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to delete recipe: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteNonFavoriteRecipes(): Result<Unit> {
        return try {
            recipeDao.deleteNonFavoriteRecipes()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to delete non-favorite recipes: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteAllRecipes(): Result<Unit> {
        return try {
            recipeDao.deleteAllRecipes()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to delete all recipes: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun getRecipeCount(): Result<Int> {
        return try {
            val count = recipeDao.getRecipeCount()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to get recipe count: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun getFavoriteRecipeCount(): Result<Int> {
        return try {
            val count = recipeDao.getFavoriteRecipeCount()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to get favorite recipe count: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    fun searchRecipes(query: String): Flow<Result<List<Recipe>>> = flow {
        try {
            recipeDao.searchRecipes(query).collect { entities ->
                emit(Result.Success(entities.map { it.toRecipe() }))
            }
        } catch (e: Exception) {
            emit(Result.Error(AppError.DatabaseError("Failed to search recipes: ${e.message}"), e.message ?: "Unknown error"))
        }
    }
} 