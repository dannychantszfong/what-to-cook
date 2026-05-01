package com.example.what_to_cook.database.dao

import androidx.room.*
import com.example.what_to_cook.database.entities.ApiKeyEntity

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys WHERE id = :id")
    suspend fun getApiKey(id: String = "user_api_key"): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity)

    @Update
    suspend fun updateApiKey(apiKey: ApiKeyEntity)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteApiKey(id: String = "user_api_key")

    @Query("SELECT EXISTS(SELECT 1 FROM api_keys WHERE id = :id)")
    suspend fun hasApiKey(id: String = "user_api_key"): Boolean
} 