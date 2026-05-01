package com.example.what_to_cook.service

import com.example.what_to_cook.data.AIModel
import com.example.what_to_cook.data.AIProvider
import com.example.what_to_cook.data.AppError
import com.example.what_to_cook.data.AuthType
import com.example.what_to_cook.data.Recipe
import com.example.what_to_cook.data.Result
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

interface AIService {
    suspend fun generateRecipes(
        ingredients: String,
        seasonings: String,
        servingSize: Int = 2,
        model: AIModel,
        apiKey: String,
        customModelName: String = "",
        customApiEndpoint: String = ""
    ): Result<List<Recipe>>
}

class AIServiceImpl : AIService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    
    // Provider handlers registry
    private val providerHandlers = mapOf(
        AIProvider.OPENAI to OpenAIHandler(),
        AIProvider.ANTHROPIC to AnthropicHandler(),
        AIProvider.GOOGLE to GoogleHandler(),
        AIProvider.COHERE to CohereHandler(),
        AIProvider.MISTRAL to MistralHandler(),
        AIProvider.ALEPH_ALPHA to AlephAlphaHandler(),
        AIProvider.BEDROCK to BedrockHandler(),
        AIProvider.AZURE to AzureHandler(),
        AIProvider.OPENROUTER to OpenRouterHandler(),
        AIProvider.DEEPSEEK to DeepSeekHandler(),
        AIProvider.GROK to GrokHandler(),
        AIProvider.HYPERBOLIC to HyperbolicHandler(),
        AIProvider.NOVITA to NovitaHandler(),
        AIProvider.TOGETHER to TogetherHandler(),
        AIProvider.FIREWORKS to FireworksHandler(),
        AIProvider.REPLICATE to ReplicateHandler(),
        AIProvider.CUSTOM to CustomHandler()
    )

    override suspend fun generateRecipes(
        ingredients: String,
        seasonings: String,
        servingSize: Int,
        model: AIModel,
        apiKey: String,
        customModelName: String,
        customApiEndpoint: String
    ): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.Error(AppError.InvalidApiKey, "API key is required")
        }

        try {
            val prompt = buildPrompt(ingredients, seasonings, servingSize)
            val handler = providerHandlers[model.provider] 
                ?: return@withContext Result.Error(AppError.ApiError("Unsupported provider"), "Provider ${model.provider} is not supported")
            
            val request = handler.buildRequest(
                prompt = prompt,
                model = model,
                apiKey = apiKey,
                customModelName = customModelName,
                customApiEndpoint = customApiEndpoint
            )
            
            executeRequest(request, handler)
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError, "Network error: ${e.message}")
        }
    }

    private fun buildPrompt(ingredients: String, seasonings: String, servingSize: Int): String {
        return """
        Based on the following ingredients and seasonings, please generate 3 diverse and practical recipes for $servingSize ${if (servingSize == 1) "person" else "people"}. 
        
        Available Ingredients: $ingredients
        Available Seasonings: $seasonings
        Serving Size: $servingSize ${if (servingSize == 1) "person" else "people"}
        
        Please respond with ONLY a valid JSON array in this exact format:
        [
          {
            "title": "Recipe Name",
            "ingredients": ["ingredient 1", "ingredient 2", "..."],
            "instructions": ["step 1", "step 2", "..."],
            "cookingTime": "X minutes",
            "servings": "$servingSize ${if (servingSize == 1) "person" else "people"}"
          }
        ]
        
        Requirements:
        - Use only the provided ingredients and seasonings
        - Scale ingredient quantities appropriately for $servingSize ${if (servingSize == 1) "person" else "people"}
        - Make recipes realistic and achievable
        - Include cooking time and servings (should be exactly "$servingSize ${if (servingSize == 1) "person" else "people"}")
        - Provide clear, step-by-step instructions
        - Make recipes diverse (different cooking methods/cuisines if possible)
        """.trimIndent()
    }

    private suspend fun executeRequest(request: Request, handler: ProviderHandler): Result<List<Recipe>> {
        return try {
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return Result.Error(
                    AppError.ApiError("HTTP ${response.code}: ${response.message}"),
                    "API request failed with code ${response.code}"
                )
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                return Result.Error(AppError.ApiError("Empty response"), "Received empty response from API")
            }

            val content = handler.extractContent(responseBody, gson)
            parseRecipesFromJson(content)
            
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError, "Request failed: ${e.message}")
        }
    }

    private fun parseRecipesFromJson(content: String): Result<List<Recipe>> {
        return try {
            // Extract JSON array from the content (in case there's extra text)
            val jsonStart = content.indexOf('[')
            val jsonEnd = content.lastIndexOf(']')
            
            if (jsonStart == -1 || jsonEnd == -1) {
                return Result.Error(AppError.ApiError("Invalid response format"), "Could not find JSON array in response")
            }
            
            val jsonContent = content.substring(jsonStart, jsonEnd + 1)
            val recipesArray = gson.fromJson(jsonContent, Array<Recipe>::class.java)
            
            if (recipesArray.isEmpty()) {
                return Result.Error(AppError.ApiError("No recipes generated"), "AI did not generate any recipes")
            }
            
            Result.Success(recipesArray.toList())
        } catch (e: Exception) {
            Result.Error(AppError.ApiError("Failed to parse response"), "Could not parse AI response: ${e.message}")
        }
    }
}

// Abstract base class for provider handlers
abstract class ProviderHandler {
    abstract fun buildRequest(
        prompt: String,
        model: AIModel,
        apiKey: String,
        customModelName: String = "",
        customApiEndpoint: String = ""
    ): Request
    
    abstract fun extractContent(responseBody: String, gson: Gson): String
    
    protected fun addAuthHeaders(builder: Request.Builder, authType: AuthType, apiKey: String): Request.Builder {
        return when (authType) {
            AuthType.BEARER_TOKEN -> builder.addHeader("Authorization", "Bearer $apiKey")
            AuthType.API_KEY_HEADER -> builder.addHeader("api-key", apiKey)
            AuthType.X_API_KEY -> builder.addHeader("x-api-key", apiKey)
            AuthType.API_KEY_PARAM -> builder // Handled in URL
            AuthType.AWS_SIGNATURE -> builder // Requires special handling
        }
    }
    
    protected fun buildStandardChatRequest(
        prompt: String,
        model: AIModel,
        apiKey: String,
        baseUrl: String,
        endpoint: String = "/chat/completions"
    ): Request {
        val requestBody = JsonObject().apply {
            addProperty("model", model.apiName)
            add("messages", Gson().toJsonTree(listOf(
                mapOf("role" to "user", "content" to prompt)
            )))
            addProperty("max_tokens", 2000)
            addProperty("temperature", 0.7)
        }

        return addAuthHeaders(
            Request.Builder()
                .url("$baseUrl$endpoint")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())),
            model.provider.authType,
            apiKey
        ).build()
    }
    
    protected fun extractStandardChatContent(responseBody: String, gson: Gson): String {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.getAsJsonArray("choices")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString ?: ""
    }
}

// OpenAI Handler
class OpenAIHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        return buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String {
        return extractStandardChatContent(responseBody, gson)
    }
}

// Anthropic Handler
class AnthropicHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        val requestBody = JsonObject().apply {
            addProperty("model", model.apiName)
            addProperty("max_tokens", 2000)
            add("messages", Gson().toJsonTree(listOf(
                mapOf("role" to "user", "content" to prompt)
            )))
        }

        return Request.Builder()
            .url("${model.provider.baseUrl}/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.getAsJsonArray("content")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString ?: ""
    }
}

// Google Handler
class GoogleHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        val requestBody = JsonObject().apply {
            add("contents", Gson().toJsonTree(listOf(
                mapOf("parts" to listOf(mapOf("text" to prompt)))
            )))
        }

        return Request.Builder()
            .url("${model.provider.baseUrl}/models/${model.apiName}:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.getAsJsonArray("candidates")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString ?: ""
    }
}

// Cohere Handler
class CohereHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        val requestBody = JsonObject().apply {
            addProperty("model", model.apiName)
            addProperty("message", prompt)
            addProperty("max_tokens", 2000)
            addProperty("temperature", 0.7)
        }

        return Request.Builder()
            .url("${model.provider.baseUrl}/chat")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.get("text")?.asString ?: ""
    }
}

// Standard OpenAI-compatible handlers (most providers use this format)
class MistralHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class AlephAlphaHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class BedrockHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        // Note: AWS Bedrock requires AWS signature authentication, which is complex to implement
        // For now, we'll use a simplified approach - in production, you'd want to use AWS SDK
        return buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    }
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class AzureHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        val endpoint = if (customApiEndpoint.isNotBlank()) customApiEndpoint else model.provider.baseUrl
        return Request.Builder()
            .url("$endpoint/openai/deployments/${model.apiName}/chat/completions?api-version=2023-12-01-preview")
            .addHeader("api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(JsonObject().apply {
                add("messages", Gson().toJsonTree(listOf(
                    mapOf("role" to "user", "content" to prompt)
                )))
                addProperty("max_tokens", 2000)
                addProperty("temperature", 0.7)
            }.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class OpenRouterHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class DeepSeekHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class GrokHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class HyperbolicHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class NovitaHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class TogetherHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class FireworksHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request =
        buildStandardChatRequest(prompt, model, apiKey, model.provider.baseUrl)
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
}

class ReplicateHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        // Replicate has a different API structure
        val requestBody = JsonObject().apply {
            add("input", JsonObject().apply {
                addProperty("prompt", prompt)
                addProperty("max_tokens", 2000)
                addProperty("temperature", 0.7)
            })
        }

        return Request.Builder()
            .url("${model.provider.baseUrl}/predictions")
            .addHeader("Authorization", "Token $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.get("output")?.asString ?: ""
    }
}

class CustomHandler : ProviderHandler() {
    override fun buildRequest(prompt: String, model: AIModel, apiKey: String, customModelName: String, customApiEndpoint: String): Request {
        if (customApiEndpoint.isBlank() || customModelName.isBlank()) {
            throw IllegalArgumentException("Custom model name and endpoint are required")
        }
        
        val requestBody = JsonObject().apply {
            addProperty("model", customModelName)
            add("messages", Gson().toJsonTree(listOf(
                mapOf("role" to "user", "content" to prompt)
            )))
            addProperty("max_tokens", 2000)
            addProperty("temperature", 0.7)
        }

        return Request.Builder()
            .url(customApiEndpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    override fun extractContent(responseBody: String, gson: Gson): String = extractStandardChatContent(responseBody, gson)
} 