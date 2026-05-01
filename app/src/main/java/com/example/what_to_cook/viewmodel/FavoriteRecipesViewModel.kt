package com.example.what_to_cook.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.what_to_cook.data.Recipe
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.WhatToCookDatabase
import com.example.what_to_cook.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteRecipesUiState(
    val favoriteRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val editingRecipe: Recipe? = null
)

class FavoriteRecipesViewModel(application: Application) : AndroidViewModel(application) {
    private val recipeRepository: RecipeRepository
    
    private val _uiState = MutableStateFlow(FavoriteRecipesUiState())
    val uiState: StateFlow<FavoriteRecipesUiState> = _uiState.asStateFlow()
    
    // Individual properties for backward compatibility
    var favoriteRecipes by mutableStateOf<List<Recipe>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        val database = WhatToCookDatabase.getDatabase(application)
        recipeRepository = RecipeRepository(database.recipeDao())
        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            isLoading = true
            
            recipeRepository.getFavoriteRecipes().collect { result ->
                when (result) {
                    is Result.Success -> {
                        favoriteRecipes = result.data
                        _uiState.value = _uiState.value.copy(
                            favoriteRecipes = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Result.Error -> {
                        errorMessage = result.message
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
            isLoading = false
        }
    }

    fun removeFavorite(recipe: Recipe) {
        viewModelScope.launch {
            try {
                when (val result = recipeRepository.updateFavoriteStatus(recipe.id, false)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Removed from favorites",
                            errorMessage = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to remove from favorites: ${result.message}"
                        )
                        errorMessage = "Failed to remove from favorites: ${result.message}"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove from favorites: ${e.message}"
                )
                errorMessage = "Failed to remove from favorites: ${e.message}"
            }
        }
    }

    fun startEditingRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(editingRecipe = recipe)
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingRecipe = null)
    }

    fun updateRecipe(
        recipe: Recipe,
        newTitle: String,
        newIngredients: List<String>,
        newInstructions: List<String>,
        newCookingTime: String,
        newServings: String
    ) {
        if (newTitle.isBlank() || newIngredients.isEmpty() || newInstructions.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            isLoading = true
            
            try {
                val updatedRecipe = recipe.copy(
                    title = newTitle.trim(),
                    ingredients = newIngredients.map { it.trim() }.filter { it.isNotBlank() },
                    instructions = newInstructions.map { it.trim() }.filter { it.isNotBlank() },
                    cookingTime = newCookingTime.trim(),
                    servings = newServings.trim()
                )
                
                when (val result = recipeRepository.updateRecipe(
                    updatedRecipe, 
                    recipe.id, 
                    "Edited favorite recipe", 
                    true
                )) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            editingRecipe = null,
                            successMessage = "Recipe updated successfully",
                            errorMessage = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to update recipe: ${result.message}"
                        )
                        errorMessage = "Failed to update recipe: ${result.message}"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update recipe: ${e.message}"
                )
                errorMessage = "Failed to update recipe: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
        errorMessage = null
    }
} 