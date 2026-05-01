package com.example.what_to_cook.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.what_to_cook.data.Ingredient
import com.example.what_to_cook.data.IngredientInput
import com.example.what_to_cook.data.IngredientType
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.WhatToCookDatabase
import com.example.what_to_cook.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class IngredientsUiState(
    val regularIngredients: List<Ingredient> = emptyList(),
    val seasonings: List<Ingredient> = emptyList(),
    val servingSize: Int = 2, // Default serving size
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inputValidationErrors: List<String> = emptyList(),
    val editingIngredient: Ingredient? = null
)

class IngredientsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IngredientRepository
    
    private val _uiState = MutableStateFlow(IngredientsUiState())
    val uiState: StateFlow<IngredientsUiState> = _uiState.asStateFlow()

    // Individual state properties for backward compatibility
    val ingredients: StateFlow<List<Ingredient>> get() = MutableStateFlow(_uiState.value.regularIngredients).asStateFlow()
    val seasonings: StateFlow<List<Ingredient>> get() = MutableStateFlow(_uiState.value.seasonings).asStateFlow()
    var isLoading by mutableStateOf(false)
        private set

    init {
        val database = WhatToCookDatabase.getDatabase(application)
        repository = IngredientRepository(database.ingredientDao())
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Load regular ingredients
            repository.getIngredientsByType(IngredientType.REGULAR).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            regularIngredients = result.data,
                            isLoading = false
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
        
        viewModelScope.launch {
            // Load seasonings
            repository.getIngredientsByType(IngredientType.SEASONING).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(seasonings = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    }
                    else -> {} // Loading handled by regular ingredients
                }
            }
        }
    }

    fun addIngredient(name: String, quantity: String, unit: String, type: IngredientType) {
        val input = IngredientInput(name.trim(), quantity.trim(), unit.trim(), type)
        val validation = input.validate()
        
        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(inputValidationErrors = validation.errors)
            return
        }
        
        // Clear validation errors if input is valid
        _uiState.value = _uiState.value.copy(inputValidationErrors = emptyList())
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            isLoading = true
            
            try {
                val ingredient = Ingredient(
                    id = UUID.randomUUID().toString(),
                    name = input.name,
                    quantity = input.quantity,
                    unit = input.unit,
                    type = input.type
                )
                
                when (val result = repository.insertIngredient(ingredient)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to add ingredient: ${e.message}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun removeIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            when (val result = repository.deleteIngredient(ingredient)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearIngredientsByType(type: IngredientType) {
        viewModelScope.launch {
            when (val result = repository.deleteIngredientsByType(type)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearAllIngredients() {
        viewModelScope.launch {
            when (val result = repository.deleteAllIngredients()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateIngredient(ingredient: Ingredient, name: String, quantity: String, unit: String) {
        val input = IngredientInput(name.trim(), quantity.trim(), unit.trim(), ingredient.type)
        val validation = input.validate()
        
        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(inputValidationErrors = validation.errors)
            return
        }
        
        // Clear validation errors if input is valid
        _uiState.value = _uiState.value.copy(inputValidationErrors = emptyList())
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            isLoading = true
            
            try {
                val updatedIngredient = ingredient.copy(
                    name = input.name,
                    quantity = input.quantity,
                    unit = input.unit
                )
                
                when (val result = repository.updateIngredient(updatedIngredient)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            editingIngredient = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update ingredient: ${e.message}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun startEditingIngredient(ingredient: Ingredient) {
        _uiState.value = _uiState.value.copy(
            editingIngredient = ingredient,
            inputValidationErrors = emptyList()
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            editingIngredient = null,
            inputValidationErrors = emptyList()
        )
    }

    fun getIngredientsText(): String {
        return _uiState.value.regularIngredients.joinToString(", ") { 
            buildString {
                if (it.quantity.isNotBlank()) {
                    append(it.quantity)
                    if (it.unit.isNotBlank()) {
                        append(" ${it.unit}")
                    }
                    append(" of ")
                }
                append(it.name)
            }
        }
    }

    fun getSeasoningsText(): String {
        return _uiState.value.seasonings.joinToString(", ") { 
            buildString {
                if (it.quantity.isNotBlank()) {
                    append(it.quantity)
                    if (it.unit.isNotBlank()) {
                        append(" ${it.unit}")
                    }
                    append(" of ")
                }
                append(it.name)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearValidationErrors() {
        _uiState.value = _uiState.value.copy(inputValidationErrors = emptyList())
    }

    fun updateServingSize(servings: Int) {
        if (servings > 0) {
            _uiState.value = _uiState.value.copy(servingSize = servings)
        }
    }

    fun getServingSize(): Int {
        return _uiState.value.servingSize
    }
} 