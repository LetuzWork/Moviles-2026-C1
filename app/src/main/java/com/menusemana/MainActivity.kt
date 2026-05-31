package com.menusemana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.menusemana.core.designsystem.component.MsBottomBar
import com.menusemana.core.designsystem.theme.MenuSemanaTheme
import com.menusemana.feature.meals.AddEditMealScreen
import com.menusemana.feature.meals.DetalleComidaScreen
import com.menusemana.feature.meals.MisComidasScreen
import com.menusemana.feature.onboarding.OnboardingScreen
import com.menusemana.feature.plan.MiSemanaScreen
import com.menusemana.feature.recipes.RecetasScreen
import com.menusemana.feature.recipes.RecipeDetailScreen
import com.menusemana.feature.shopping.ComprasScreen
import com.menusemana.navigation.AddMeal
import com.menusemana.navigation.EditMeal
import com.menusemana.navigation.MealDetail
import com.menusemana.navigation.Meals
import com.menusemana.navigation.Onboarding
import com.menusemana.navigation.Plan
import com.menusemana.navigation.RecipeDetail
import com.menusemana.navigation.Recipes
import com.menusemana.navigation.Shopping
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuSemanaTheme {
                MenuSemanaApp(dataStore)
            }
        }
    }
}

@Composable
private fun MenuSemanaApp(dataStore: DataStore<Preferences>) {
    val navController = rememberNavController()
    var startDestination: Any by remember { mutableStateOf(Plan) }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = dataStore.data.firstOrNull()
        val seen = prefs?.get(booleanPreferencesKey("onboarding_seen")) ?: false
        startDestination = if (seen) Plan else Onboarding
        ready = true
    }

    if (!ready) return
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val topLevelRoutes = listOf(
        Plan::class.qualifiedName,
        Meals::class.qualifiedName,
        Recipes::class.qualifiedName,
        Shopping::class.qualifiedName,
    )
    val showBottomBar = topLevelRoutes.any { currentRoute?.startsWith(it ?: "") == true }

    val selectedIndex = when {
        currentRoute?.startsWith(Plan::class.qualifiedName ?: "") == true -> 0
        currentRoute?.startsWith(Meals::class.qualifiedName ?: "") == true -> 1
        currentRoute?.startsWith(Recipes::class.qualifiedName ?: "") == true -> 2
        currentRoute?.startsWith(Shopping::class.qualifiedName ?: "") == true -> 3
        else -> 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MsBottomBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        val route = when (index) {
                            0 -> Plan
                            1 -> Meals
                            2 -> Recipes
                            else -> Shopping
                        }
                        navController.navigate(route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable<Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Plan) {
                            popUpTo<Onboarding> { inclusive = true }
                        }
                    }
                )
            }
            composable<Plan> {
                MiSemanaScreen(
                    contentPadding = innerPadding,
                    onNavigateToMealDetail = { mealId ->
                        navController.navigate(MealDetail(mealId))
                    }
                )
            }
            composable<Meals> {
                MisComidasScreen(
                    contentPadding = innerPadding,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(MealDetail(mealId))
                    },
                    onNavigateToAdd = {
                        navController.navigate(AddMeal)
                    }
                )
            }
            composable<MealDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<MealDetail>()
                DetalleComidaScreen(
                    mealId = route.mealId,
                    onNavigateUp = { navController.navigateUp() },
                    onNavigateToEdit = { navController.navigate(EditMeal(route.mealId)) },
                    onDeleted = { navController.navigateUp() },
                    onNavigateToPlan = {
                        navController.navigate(Plan) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<AddMeal> {
                AddEditMealScreen(
                    mealId = null,
                    onNavigateUp = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() },
                )
            }
            composable<EditMeal> { backStackEntry ->
                val route = backStackEntry.toRoute<EditMeal>()
                AddEditMealScreen(
                    mealId = route.mealId,
                    onNavigateUp = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() },
                )
            }
            composable<Recipes> {
                RecetasScreen(
                    contentPadding = innerPadding,
                    onNavigateToDetail = { mealDbId ->
                        navController.navigate(RecipeDetail(mealDbId))
                    }
                )
            }
            composable<RecipeDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<RecipeDetail>()
                RecipeDetailScreen(
                    mealDbId = route.mealDbId,
                    onNavigateUp = { navController.navigateUp() },
                    onNavigateToMyMeals = {
                        navController.navigate(Meals) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<Shopping> {
                ComprasScreen(contentPadding = innerPadding)
            }
        }
    }
}
