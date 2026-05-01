package com.example.what_to_cook.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.what_to_cook.data.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "recipes")
@TypeConverters(StringListConverter::class)
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTime: String,
    val servings: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceIngredients: String // The original ingredients used to generate this recipe
)

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// Extension functions to convert between data classes and entities
fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = title,
        ingredients = ingredients,
        instructions = instructions,
        cookingTime = cookingTime,
        servings = servings,
        isFavorite = isFavorite
    )
}

fun Recipe.toEntity(id: String, sourceIngredients: String, isFavorite: Boolean = false): RecipeEntity {
    return RecipeEntity(
        id = id,
        title = title,
        ingredients = ingredients,
        instructions = instructions,
        cookingTime = cookingTime,
        servings = servings,
        isFavorite = isFavorite,
        sourceIngredients = sourceIngredients
    )
} 