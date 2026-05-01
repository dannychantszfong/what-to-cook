package com.example.what_to_cook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.what_to_cook.ui.screens.IngredientsScreen
import com.example.what_to_cook.ui.screens.ProfileScreen
import com.example.what_to_cook.ui.screens.RecipeScreen
import com.example.what_to_cook.ui.theme.What_to_cookTheme
import com.example.what_to_cook.viewmodel.IngredientsViewModel
import com.example.what_to_cook.viewmodel.ProfileViewModel
import com.example.what_to_cook.viewmodel.RecipeViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Ingredients : Screen("ingredients", "Ingredients", Icons.Default.ShoppingCart)
    object Recipe : Screen("recipe", "Recipes", Icons.Default.Restaurant)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            What_to_cookTheme {
                WhatToCookApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatToCookApp() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Ingredients, Screen.Recipe, Screen.Profile)
    val context = LocalContext.current
    
    // Shared ViewModels across the app
    val ingredientsViewModel: IngredientsViewModel = viewModel { 
        IngredientsViewModel(context.applicationContext as android.app.Application) 
    }
    val recipeViewModel: RecipeViewModel = viewModel { 
        RecipeViewModel(context.applicationContext as android.app.Application) 
    }
    val profileViewModel: ProfileViewModel = viewModel { 
        ProfileViewModel(context.applicationContext as android.app.Application) 
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Ingredients.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Ingredients.route) {
                IngredientsScreen(viewModel = ingredientsViewModel)
            }
            composable(Screen.Recipe.route) {
                RecipeScreen(
                    ingredientsViewModel = ingredientsViewModel,
                    recipeViewModel = recipeViewModel,
                    profileViewModel = profileViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(profileViewModel = profileViewModel)
            }
        }
    }
}