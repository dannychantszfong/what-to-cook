package com.example.what_to_cook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.what_to_cook.data.AIModel
import com.example.what_to_cook.data.AIProvider
import com.example.what_to_cook.viewmodel.FavoriteRecipesViewModel
import com.example.what_to_cook.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val favoriteRecipesViewModel: FavoriteRecipesViewModel = viewModel()
    
    val tabs = listOf("API Settings", "Favorite Recipes")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> ApiSettingsTab(viewModel = profileViewModel)
            1 -> FavoriteRecipesScreen(viewModel = favoriteRecipesViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsTab(
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Model Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI Model Selection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedAIModel.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select AI Model") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Group models by provider
                            val modelsByProvider = AIModel.values().groupBy { it.provider }
                            
                            modelsByProvider.forEach { (provider, models) ->
                                // Provider header
                                Text(
                                    text = provider.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = "  ${model.displayName}",
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        },
                                        onClick = {
                                            viewModel.updateAIModel(model, context)
                                            expanded = false
                                        }
                                    )
                                }
                                
                                if (provider != AIProvider.CUSTOM) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }

                    Text(
                        text = "Selected: ${viewModel.selectedAIModel.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Text(
                        text = "Provider: ${viewModel.selectedAIModel.provider.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // API Key Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "API Configuration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.apiKey,
                        onValueChange = { viewModel.updateApiKey(it, context) },
                        label = { Text("API Key") },
                        placeholder = { Text("Enter your ${viewModel.selectedAIModel.provider.displayName} API key") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = if (passwordVisible) "Hide API key" else "Show API key"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Your API key is stored securely on your device and never shared.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Custom Model Configuration (show for Custom or Azure which need endpoints)
        if (viewModel.selectedAIModel == AIModel.CUSTOM || 
            viewModel.selectedAIModel.provider == AIProvider.AZURE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (viewModel.selectedAIModel == AIModel.CUSTOM) 
                                "Custom Model Configuration" 
                            else 
                                "Azure Configuration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (viewModel.selectedAIModel == AIModel.CUSTOM) {
                            OutlinedTextField(
                                value = viewModel.customModelName,
                                onValueChange = { viewModel.updateCustomModelName(it, context) },
                                label = { Text("Model Name") },
                                placeholder = { Text("e.g., gpt-4, claude-3, custom-model") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = viewModel.customApiEndpoint,
                            onValueChange = { viewModel.updateCustomApiEndpoint(it, context) },
                            label = { 
                                Text(
                                    if (viewModel.selectedAIModel.provider == AIProvider.AZURE) 
                                        "Azure Resource Endpoint" 
                                    else 
                                        "API Endpoint"
                                )
                            },
                            placeholder = { 
                                Text(
                                    if (viewModel.selectedAIModel.provider == AIProvider.AZURE)
                                        "https://your-resource.openai.azure.com"
                                    else
                                        "https://api.example.com/v1/chat/completions"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Status indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = if (viewModel.apiKey.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                
                val statusText = if (viewModel.apiKey.isNotBlank()) {
                    "✓ API Key configured for ${viewModel.selectedAIModel.provider.displayName}"
                } else {
                    "⚠ API Key required for ${viewModel.selectedAIModel.provider.displayName}"
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 