package com.example.what_to_cook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.what_to_cook.data.Ingredient
import com.example.what_to_cook.data.IngredientType
import com.example.what_to_cook.viewmodel.IngredientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    viewModel: IngredientsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var regularIngredientName by remember { mutableStateOf("") }
    var regularQuantity by remember { mutableStateOf("") }
    var regularSelectedUnit by remember { mutableStateOf("") }
    var regularExpanded by remember { mutableStateOf(false) }
    
    var seasoningName by remember { mutableStateOf("") }
    var seasoningQuantity by remember { mutableStateOf("") }
    var seasoningSelectedUnit by remember { mutableStateOf("") }
    var seasoningExpanded by remember { mutableStateOf(false) }
    
    // Edit mode states
    var editName by remember { mutableStateOf("") }
    var editQuantity by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("") }
    var editExpanded by remember { mutableStateOf(false) }
    
    // Update edit fields when editing ingredient changes
    LaunchedEffect(uiState.editingIngredient) {
        uiState.editingIngredient?.let { ingredient ->
            editName = ingredient.name
            editQuantity = ingredient.quantity
            editUnit = ingredient.unit
        }
    }
    
    val units = listOf(
        "", "grams", "kg", "portions", "pieces", "cups", "tablespoons", 
        "teaspoons", "liters", "ml", "oz", "lbs", "pinch", "dash"
    )
    val seasoningUnits = listOf(
        "", "pinch", "dash", "teaspoons", "tablespoons", "grams", "ml", 
        "pieces", "drops", "cloves", "sprigs"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Manage Your Ingredients",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Serving Size Selector
        item {
            ServingSizeCard(
                servingSize = uiState.servingSize,
                onServingSizeChange = { viewModel.updateServingSize(it) }
            )
        }

        // Error display
        uiState.errorMessage?.let { errorMessage ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Validation errors
        if (uiState.inputValidationErrors.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Please fix the following errors:",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        uiState.inputValidationErrors.forEach { error ->
                            Text(
                                text = "• $error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Regular Ingredients Section
        item {
            Text(
                text = "Regular Ingredients",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            IngredientInputCard(
                ingredientName = regularIngredientName,
                onIngredientNameChange = { regularIngredientName = it },
                quantity = regularQuantity,
                onQuantityChange = { regularQuantity = it },
                selectedUnit = regularSelectedUnit,
                onUnitChange = { regularSelectedUnit = it },
                expanded = regularExpanded,
                onExpandedChange = { regularExpanded = it },
                units = units,
                onAddClick = {
                    if (regularIngredientName.isNotBlank()) {
                        viewModel.addIngredient(
                            regularIngredientName,
                            regularQuantity,
                            regularSelectedUnit,
                            IngredientType.REGULAR
                        )
                        regularIngredientName = ""
                        regularQuantity = ""
                        regularSelectedUnit = ""
                    }
                },
                isLoading = uiState.isLoading,
                cardTitle = "Add Regular Ingredient"
            )
        }

        if (uiState.regularIngredients.isNotEmpty()) {
            item {
                Text(
                    text = "Your Regular Ingredients:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            items(uiState.regularIngredients) { ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    onDelete = { viewModel.removeIngredient(ingredient) },
                    onEdit = { viewModel.startEditingIngredient(ingredient) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.clearIngredientsByType(IngredientType.REGULAR) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Regular Ingredients")
                }
            }
        }

        // Seasonings Section
        item {
            Text(
                text = "Seasonings & Spices",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            IngredientInputCard(
                ingredientName = seasoningName,
                onIngredientNameChange = { seasoningName = it },
                quantity = seasoningQuantity,
                onQuantityChange = { seasoningQuantity = it },
                selectedUnit = seasoningSelectedUnit,
                onUnitChange = { seasoningSelectedUnit = it },
                expanded = seasoningExpanded,
                onExpandedChange = { seasoningExpanded = it },
                units = seasoningUnits,
                onAddClick = {
                    if (seasoningName.isNotBlank()) {
                        viewModel.addIngredient(
                            seasoningName,
                            seasoningQuantity,
                            seasoningSelectedUnit,
                            IngredientType.SEASONING
                        )
                        seasoningName = ""
                        seasoningQuantity = ""
                        seasoningSelectedUnit = ""
                    }
                },
                isLoading = uiState.isLoading,
                cardTitle = "Add Seasoning"
            )
        }

        if (uiState.seasonings.isNotEmpty()) {
            item {
                Text(
                    text = "Your Seasonings:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            items(uiState.seasonings) { seasoning ->
                IngredientItem(
                    ingredient = seasoning,
                    onDelete = { viewModel.removeIngredient(seasoning) },
                    onEdit = { viewModel.startEditingIngredient(seasoning) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.clearIngredientsByType(IngredientType.SEASONING) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Seasonings")
                }
            }
        }

        // Clear all button
        if (uiState.regularIngredients.isNotEmpty() || uiState.seasonings.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.clearAllIngredients() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All Ingredients & Seasonings")
                }
            }
        }

        // Empty state
        if (uiState.regularIngredients.isEmpty() && uiState.seasonings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ingredients added yet.\nStart adding your available ingredients and seasonings!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
    
    // Edit Ingredient Dialog
    uiState.editingIngredient?.let { editingIngredient ->
        EditIngredientDialog(
            ingredient = editingIngredient,
            name = editName,
            onNameChange = { editName = it },
            quantity = editQuantity,
            onQuantityChange = { editQuantity = it },
            unit = editUnit,
            onUnitChange = { editUnit = it },
            expanded = editExpanded,
            onExpandedChange = { editExpanded = it },
            units = if (editingIngredient.type == IngredientType.SEASONING) seasoningUnits else units,
            isLoading = uiState.isLoading,
            onSave = { 
                viewModel.updateIngredient(editingIngredient, editName, editQuantity, editUnit)
            },
            onCancel = { viewModel.cancelEditing() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientInputCard(
    ingredientName: String,
    onIngredientNameChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    selectedUnit: String,
    onUnitChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    units: List<String>,
    onAddClick: () -> Unit,
    isLoading: Boolean,
    cardTitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = cardTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = ingredientName,
                onValueChange = onIngredientNameChange,
                label = { Text("Ingredient Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity (Optional)") },
                    modifier = Modifier.weight(1f)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { onExpandedChange(false) }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (unit.isBlank()) "No unit" else unit
                                    )
                                },
                                onClick = {
                                    onUnitChange(unit)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onAddClick,
                enabled = !isLoading,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientDialog(
    ingredient: Ingredient,
    name: String,
    onNameChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    units: List<String>,
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Edit ${if (ingredient.type == IngredientType.SEASONING) "Seasoning" else "Ingredient"}",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { onExpandedChange(false) }
                    ) {
                        units.forEach { unitOption ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (unitOption.isBlank()) "No unit" else unitOption
                                    )
                                },
                                onClick = {
                                    onUnitChange(unitOption)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !isLoading && name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Row {
                    Text(
                        text = buildString {
                            append(ingredient.quantity)
                            if (ingredient.unit.isNotBlank()) {
                                append(" ${ingredient.unit}")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    if (ingredient.type == IngredientType.SEASONING) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seasoning",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "Tap to edit",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ingredient",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServingSizeCard(
    servingSize: Int,
    onServingSizeChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val servingSizes = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Serving Size",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "How many people are you cooking for?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = "$servingSize ${if (servingSize == 1) "person" else "people"}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Serving Size") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    servingSizes.forEach { size ->
                        DropdownMenuItem(
                            text = { 
                                Text("$size ${if (size == 1) "person" else "people"}")
                            },
                            onClick = {
                                onServingSizeChange(size)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
} 