package com.example.what_to_cook.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.what_to_cook.data.Ingredient
import com.example.what_to_cook.data.IngredientType

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val quantity: String,
    val unit: String,
    val type: String = IngredientType.REGULAR.name, // REGULAR or SEASONING
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey
    val id: String = "user_api_key",
    val aiModel: String,
    val apiKey: String,
    val customModelName: String = "",
    val customApiEndpoint: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions to convert between data classes and entities
fun IngredientEntity.toIngredient(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        type = IngredientType.valueOf(type)
    )
}

fun Ingredient.toEntity(): IngredientEntity {
    return IngredientEntity(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        type = type.name
    )
} 