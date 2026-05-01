package com.example.what_to_cook.repository

import com.example.what_to_cook.data.AIModel
import com.example.what_to_cook.data.AppError
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.dao.ApiKeyDao
import com.example.what_to_cook.database.entities.ApiKeyEntity

class ApiKeyRepository(
    private val apiKeyDao: ApiKeyDao
) {
    suspend fun getApiKey(): Result<ApiKeyEntity?> {
        return try {
            val apiKey = apiKeyDao.getApiKey()
            Result.Success(apiKey)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to load API key: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun saveApiKey(
        aiModel: AIModel,
        apiKey: String,
        customModelName: String = "",
        customApiEndpoint: String = ""
    ): Result<Unit> {
        return try {
            val entity = ApiKeyEntity(
                aiModel = aiModel.name,
                apiKey = apiKey,
                customModelName = customModelName,
                customApiEndpoint = customApiEndpoint,
                updatedAt = System.currentTimeMillis()
            )
            apiKeyDao.insertApiKey(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to save API key: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun updateApiKey(
        aiModel: AIModel,
        apiKey: String,
        customModelName: String = "",
        customApiEndpoint: String = ""
    ): Result<Unit> {
        return try {
            val entity = ApiKeyEntity(
                aiModel = aiModel.name,
                apiKey = apiKey,
                customModelName = customModelName,
                customApiEndpoint = customApiEndpoint,
                updatedAt = System.currentTimeMillis()
            )
            apiKeyDao.updateApiKey(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to update API key: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun deleteApiKey(): Result<Unit> {
        return try {
            apiKeyDao.deleteApiKey()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to delete API key: ${e.message}"), e.message ?: "Unknown error")
        }
    }

    suspend fun hasApiKey(): Result<Boolean> {
        return try {
            val hasKey = apiKeyDao.hasApiKey()
            Result.Success(hasKey)
        } catch (e: Exception) {
            Result.Error(AppError.DatabaseError("Failed to check API key existence: ${e.message}"), e.message ?: "Unknown error")
        }
    }
} 