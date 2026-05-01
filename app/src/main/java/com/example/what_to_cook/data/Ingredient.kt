package com.example.what_to_cook.data

data class Ingredient(
    val id: String = "",
    val name: String,
    val quantity: String = "", // Optional: quantity amount
    val unit: String = "", // Optional: "grams", "portions", "pieces", etc.
    val type: IngredientType = IngredientType.REGULAR
)

enum class IngredientType {
    REGULAR,
    SEASONING
}

data class Recipe(
    val id: String = "",
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTime: String,
    val servings: String,
    val isFavorite: Boolean = false
)

enum class AIModel(
    val displayName: String, 
    val apiName: String, 
    val provider: AIProvider,
    val requiresApiKey: Boolean = true,
    val defaultEndpoint: String = ""
) {
    // OpenAI Models
    OPENAI_GPT4("OpenAI GPT-4", "gpt-4", AIProvider.OPENAI),
    OPENAI_GPT4_TURBO("OpenAI GPT-4 Turbo", "gpt-4-turbo", AIProvider.OPENAI),
    OPENAI_GPT35_TURBO("OpenAI GPT-3.5 Turbo", "gpt-3.5-turbo", AIProvider.OPENAI),
    
    // Anthropic Models
    CLAUDE_3_OPUS("Claude 3 Opus", "claude-3-opus-20240229", AIProvider.ANTHROPIC),
    CLAUDE_3_SONNET("Claude 3 Sonnet", "claude-3-sonnet-20240229", AIProvider.ANTHROPIC),
    CLAUDE_3_HAIKU("Claude 3 Haiku", "claude-3-haiku-20240307", AIProvider.ANTHROPIC),
    
    // Google Models
    GEMINI_PRO("Google Gemini Pro", "gemini-pro", AIProvider.GOOGLE),
    GEMINI_PRO_VISION("Google Gemini Pro Vision", "gemini-pro-vision", AIProvider.GOOGLE),
    
    // Cohere Models
    COHERE_COMMAND("Cohere Command", "command", AIProvider.COHERE),
    COHERE_COMMAND_R("Cohere Command R", "command-r", AIProvider.COHERE),
    COHERE_COMMAND_R_PLUS("Cohere Command R+", "command-r-plus", AIProvider.COHERE),
    
    // Mistral Models
    MISTRAL_LARGE("Mistral Large", "mistral-large-latest", AIProvider.MISTRAL),
    MISTRAL_MEDIUM("Mistral Medium", "mistral-medium-latest", AIProvider.MISTRAL),
    MISTRAL_SMALL("Mistral Small", "mistral-small-latest", AIProvider.MISTRAL),
    
    // Aleph Alpha Models
    ALEPH_ALPHA_LUMINOUS("Aleph Alpha Luminous", "luminous-extended", AIProvider.ALEPH_ALPHA),
    
    // AWS Bedrock Models
    BEDROCK_CLAUDE("AWS Bedrock Claude", "anthropic.claude-3-sonnet-20240229-v1:0", AIProvider.BEDROCK),
    BEDROCK_TITAN("AWS Bedrock Titan", "amazon.titan-text-express-v1", AIProvider.BEDROCK),
    
    // Azure OpenAI Models
    AZURE_GPT4("Azure OpenAI GPT-4", "gpt-4", AIProvider.AZURE),
    AZURE_GPT35("Azure OpenAI GPT-3.5", "gpt-35-turbo", AIProvider.AZURE),
    
    // OpenRouter Models
    OPENROUTER_AUTO("OpenRouter Auto", "openrouter/auto", AIProvider.OPENROUTER),
    OPENROUTER_GPT4("OpenRouter GPT-4", "openai/gpt-4", AIProvider.OPENROUTER),
    OPENROUTER_CLAUDE("OpenRouter Claude", "anthropic/claude-3-sonnet", AIProvider.OPENROUTER),
    
    // DeepSeek Models
    DEEPSEEK_CHAT("DeepSeek Chat", "deepseek-chat", AIProvider.DEEPSEEK),
    DEEPSEEK_CODER("DeepSeek Coder", "deepseek-coder", AIProvider.DEEPSEEK),
    
    // Grok Models
    GROK_BETA("Grok Beta", "grok-beta", AIProvider.GROK),
    
    // Hyperbolic Models
    HYPERBOLIC_LLAMA("Hyperbolic Llama", "meta-llama/Llama-2-70b-chat-hf", AIProvider.HYPERBOLIC),
    
    // Novita Models
    NOVITA_LLAMA("Novita Llama", "meta-llama/llama-2-70b-chat", AIProvider.NOVITA),
    
    // Together Models
    TOGETHER_LLAMA("Together Llama", "meta-llama/Llama-2-70b-chat-hf", AIProvider.TOGETHER),
    TOGETHER_MIXTRAL("Together Mixtral", "mistralai/Mixtral-8x7B-Instruct-v0.1", AIProvider.TOGETHER),
    
    // Fireworks Models
    FIREWORKS_LLAMA("Fireworks Llama", "accounts/fireworks/models/llama-v2-70b-chat", AIProvider.FIREWORKS),
    FIREWORKS_MIXTRAL("Fireworks Mixtral", "accounts/fireworks/models/mixtral-8x7b-instruct", AIProvider.FIREWORKS),
    
    // Replicate Models
    REPLICATE_LLAMA("Replicate Llama", "meta/llama-2-70b-chat", AIProvider.REPLICATE),
    REPLICATE_MISTRAL("Replicate Mistral", "mistralai/mixtral-8x7b-instruct-v0.1", AIProvider.REPLICATE),
    
    // Custom Model
    CUSTOM("Custom Model", "custom", AIProvider.CUSTOM)
}

enum class AIProvider(
    val displayName: String,
    val baseUrl: String,
    val authType: AuthType = AuthType.BEARER_TOKEN,
    val requiresCustomEndpoint: Boolean = false
) {
    OPENAI("OpenAI", "https://api.openai.com/v1", AuthType.BEARER_TOKEN),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1", AuthType.X_API_KEY),
    GOOGLE("Google", "https://generativelanguage.googleapis.com/v1", AuthType.API_KEY_PARAM),
    COHERE("Cohere", "https://api.cohere.ai/v1", AuthType.BEARER_TOKEN),
    MISTRAL("Mistral AI", "https://api.mistral.ai/v1", AuthType.BEARER_TOKEN),
    ALEPH_ALPHA("Aleph Alpha", "https://api.aleph-alpha.com", AuthType.BEARER_TOKEN),
    BEDROCK("AWS Bedrock", "https://bedrock-runtime.{region}.amazonaws.com", AuthType.AWS_SIGNATURE),
    AZURE("Azure OpenAI", "https://{resource}.openai.azure.com", AuthType.API_KEY_HEADER, true),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1", AuthType.BEARER_TOKEN),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1", AuthType.BEARER_TOKEN),
    GROK("Grok", "https://api.grok.x.ai/v1", AuthType.BEARER_TOKEN),
    HYPERBOLIC("Hyperbolic", "https://api.hyperbolic.xyz/v1", AuthType.BEARER_TOKEN),
    NOVITA("Novita", "https://api.novita.ai/v3", AuthType.BEARER_TOKEN),
    TOGETHER("Together AI", "https://api.together.xyz/v1", AuthType.BEARER_TOKEN),
    FIREWORKS("Fireworks AI", "https://api.fireworks.ai/inference/v1", AuthType.BEARER_TOKEN),
    REPLICATE("Replicate", "https://api.replicate.com/v1", AuthType.BEARER_TOKEN),
    CUSTOM("Custom", "", AuthType.BEARER_TOKEN, true)
}

enum class AuthType {
    BEARER_TOKEN,      // Authorization: Bearer {token}
    API_KEY_HEADER,    // api-key: {key}
    X_API_KEY,         // x-api-key: {key}
    API_KEY_PARAM,     // ?key={key}
    AWS_SIGNATURE      // AWS Signature V4
}

// Error handling classes
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : Result<T>()
    data class Loading<T>(val placeholder: T? = null) : Result<T>()
}

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    object NetworkError : AppError("Network connection failed")
    object InvalidApiKey : AppError("Invalid API key provided")
    data class ApiError(val errorMessage: String) : AppError("API Error: $errorMessage")
    data class DatabaseError(val errorMessage: String) : AppError("Database Error: $errorMessage")
    data class ValidationError(val field: String, val errorMessage: String) : AppError("Validation Error: $errorMessage")
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

data class IngredientInput(
    val name: String,
    val quantity: String,
    val unit: String,
    val type: IngredientType
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Name cannot be empty")
        } else if (name.length > 50) {
            errors.add("Name must be less than 50 characters")
        }
        
        // Quantity is now optional - only validate if provided
        if (quantity.isNotBlank()) {
            try {
                val numQuantity = quantity.toDoubleOrNull()
                if (numQuantity == null || numQuantity <= 0) {
                    errors.add("Quantity must be a positive number")
                }
            } catch (e: NumberFormatException) {
                errors.add("Quantity must be a valid number")
            }
        }
        
        // Unit is optional - no validation needed
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
} 