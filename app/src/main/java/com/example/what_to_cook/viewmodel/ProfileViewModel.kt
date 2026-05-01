package com.example.what_to_cook.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.what_to_cook.data.AIModel
import com.example.what_to_cook.data.Result
import com.example.what_to_cook.database.WhatToCookDatabase
import com.example.what_to_cook.repository.ApiKeyRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val selectedAIModel: AIModel = AIModel.OPENAI_GPT35_TURBO,
    val apiKey: String = "",
    val customModelName: String = "",
    val customApiEndpoint: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKeyRepository: ApiKeyRepository
    
    private var _uiState = mutableStateOf(ProfileUiState())
    val uiState = _uiState
    
    // Individual properties for backward compatibility
    var selectedAIModel by mutableStateOf(AIModel.OPENAI_GPT35_TURBO)
        private set
    
    var apiKey by mutableStateOf("")
        private set
    
    var customModelName by mutableStateOf("")
        private set
    
    var customApiEndpoint by mutableStateOf("")
        private set

    init {
        val database = WhatToCookDatabase.getDatabase(application)
        apiKeyRepository = ApiKeyRepository(database.apiKeyDao())
    }

    fun loadSettings(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = apiKeyRepository.getApiKey()) {
                is Result.Success -> {
                    val apiKeyEntity = result.data
                    if (apiKeyEntity != null) {
                        val model = AIModel.values().find { it.name == apiKeyEntity.aiModel } ?: AIModel.OPENAI_GPT35_TURBO
                        
                        selectedAIModel = model
                        apiKey = apiKeyEntity.apiKey
                        customModelName = apiKeyEntity.customModelName
                        customApiEndpoint = apiKeyEntity.customApiEndpoint
                        
                        _uiState.value = _uiState.value.copy(
                            selectedAIModel = model,
                            apiKey = apiKeyEntity.apiKey,
                            customModelName = apiKeyEntity.customModelName,
                            customApiEndpoint = apiKeyEntity.customApiEndpoint,
                            isLoading = false
                        )
                    } else {
                        // No API key stored, use defaults
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load settings: ${result.message}"
                    )
                }
                else -> {}
            }
        }
    }

    fun updateAIModel(model: AIModel, context: Context) {
        selectedAIModel = model
        _uiState.value = _uiState.value.copy(selectedAIModel = model)
        saveSettings(context)
    }

    fun updateApiKey(key: String, context: Context) {
        apiKey = key
        _uiState.value = _uiState.value.copy(apiKey = key)
        saveSettings(context)
    }

    fun updateCustomModelName(name: String, context: Context) {
        customModelName = name
        _uiState.value = _uiState.value.copy(customModelName = name)
        saveSettings(context)
    }

    fun updateCustomApiEndpoint(endpoint: String, context: Context) {
        customApiEndpoint = endpoint
        _uiState.value = _uiState.value.copy(customApiEndpoint = endpoint)
        saveSettings(context)
    }

    private fun saveSettings(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            
            when (val result = apiKeyRepository.saveApiKey(
                aiModel = selectedAIModel,
                apiKey = apiKey,
                customModelName = customModelName,
                customApiEndpoint = customApiEndpoint
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Settings saved successfully"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to save settings: ${result.message}"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearApiKey(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = apiKeyRepository.deleteApiKey()) {
                is Result.Success -> {
                    selectedAIModel = AIModel.OPENAI_GPT35_TURBO
                    apiKey = ""
                    customModelName = ""
                    customApiEndpoint = ""
                    
                    _uiState.value = _uiState.value.copy(
                        selectedAIModel = AIModel.OPENAI_GPT35_TURBO,
                        apiKey = "",
                        customModelName = "",
                        customApiEndpoint = "",
                        isLoading = false,
                        successMessage = "API key cleared successfully"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to clear API key: ${result.message}"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
} 