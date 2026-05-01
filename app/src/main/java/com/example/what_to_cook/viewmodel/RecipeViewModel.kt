package com.example.what_to_cook.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.what_to_cook.data.AIModel
import com.example.what_to_cook.data.Recipe
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.WhatToCookDatabase
import com.example.what_to_cook.repository.ApiKeyRepository
import com.example.what_to_cook.repository.RecipeRepository
import com.example.what_to_cook.service.AIService
import com.example.what_to_cook.service.AIServiceImpl
import kotlinx.coroutines.launch

data class RecipeUiState(
    val generatedRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val recipeRepository: RecipeRepository
    private val apiKeyRepository: ApiKeyRepository
    private val aiService: AIService = AIServiceImpl()
    
    private var _uiState = mutableStateOf(RecipeUiState())
    val uiState = _uiState
    
    // Properties for backward compatibility
    var isLoading by mutableStateOf(false)
        private set
    
    var generatedRecipes by mutableStateOf<List<Recipe>>(emptyList())
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        val database = WhatToCookDatabase.getDatabase(application)
        recipeRepository = RecipeRepository(database.recipeDao())
        apiKeyRepository = ApiKeyRepository(database.apiKeyDao())
    }

    fun generateRecipes(ingredients: String, seasonings: String, servingSize: Int = 2) {
        if (ingredients.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please add some ingredients first"
            )
            errorMessage = "Please add some ingredients first"
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            isLoading = true
            errorMessage = null
            
            try {
                // Get API key from database
                when (val apiKeyResult = apiKeyRepository.getApiKey()) {
                    is Result.Success -> {
                        val apiKeyEntity = apiKeyResult.data
                        if (apiKeyEntity == null || apiKeyEntity.apiKey.isBlank()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Please configure your API key in the Profile section first"
                            )
                            isLoading = false
                            errorMessage = "Please configure your API key in the Profile section first"
                            return@launch
                        }

                        val aiModel = AIModel.values().find { it.name == apiKeyEntity.aiModel } ?: AIModel.OPENAI_GPT35_TURBO
                        
                        // Generate recipes using AI service
                        when (val result = aiService.generateRecipes(
                            ingredients = ingredients,
                            seasonings = seasonings,
                            servingSize = servingSize,
                            model = aiModel,
                            apiKey = apiKeyEntity.apiKey,
                            customModelName = apiKeyEntity.customModelName,
                            customApiEndpoint = apiKeyEntity.customApiEndpoint
                        )) {
                            is Result.Success -> {
                                val recipesWithIds = result.data.map { recipe ->
                                    recipe.copy(id = java.util.UUID.randomUUID().toString())
                                }
                                generatedRecipes = recipesWithIds
                                
                                _uiState.value = _uiState.value.copy(
                                    generatedRecipes = recipesWithIds,
                                    isLoading = false,
                                    successMessage = "Generated ${recipesWithIds.size} recipes successfully!"
                                )
                                
                                // Save recipes to database
                                val sourceIngredients = buildString {
                                    append("Ingredients: $ingredients")
                                    if (seasonings.isNotBlank()) {
                                        append(", Seasonings: $seasonings")
                                    }
                                }
                                
                                when (val saveResult = recipeRepository.insertRecipes(recipesWithIds, sourceIngredients)) {
                                    is Result.Error -> {
                                        // Don't fail the whole operation if saving fails
                                        _uiState.value = _uiState.value.copy(
                                            successMessage = "Generated ${recipesWithIds.size} recipes successfully! (Note: Could not save to database)"
                                        )
                                    }
                                    else -> {}
                                }
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = result.message
                                )
                                errorMessage = result.message
                            }
                            else -> {}
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load API configuration: ${apiKeyResult.message}"
                        )
                        errorMessage = "Failed to load API configuration: ${apiKeyResult.message}"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
                errorMessage = "Unexpected error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearRecipes() {
        generatedRecipes = emptyList()
        errorMessage = null
        _uiState.value = _uiState.value.copy(
            generatedRecipes = emptyList(),
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
        errorMessage = null
    }

    fun retryGeneration(ingredients: String, seasonings: String, servingSize: Int = 2) {
        generateRecipes(ingredients, seasonings, servingSize)
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            try {
                val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
                when (val result = recipeRepository.updateFavoriteStatus(recipe.id, updatedRecipe.isFavorite)) {
                    is Result.Success -> {
                        // Update the local list
                        val updatedList = generatedRecipes.map { 
                            if (it.id == recipe.id) updatedRecipe else it 
                        }
                        generatedRecipes = updatedList
                        
                        _uiState.value = _uiState.value.copy(
                            generatedRecipes = updatedList
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to update favorite: ${result.message}"
                        )
                        errorMessage = "Failed to update favorite: ${result.message}"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update favorite: ${e.message}"
                )
                errorMessage = "Failed to update favorite: ${e.message}"
            }
        }
    }
} 