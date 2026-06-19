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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.menusemana.screens.AddMeal
import com.menusemana.screens.EditMeal
import com.menusemana.screens.MealDetail
import com.menusemana.screens.Meals
import com.menusemana.screens.Onboarding
import com.menusemana.screens.Plan
import com.menusemana.screens.Profile
import com.menusemana.screens.RecipeDetail
import com.menusemana.screens.Recipes
import com.menusemana.screens.Shopping
import com.menusemana.screens.meals.AddEditMealScreen
import com.menusemana.screens.meals.DetalleComidaScreen
import com.menusemana.screens.meals.MisComidasScreen
import com.menusemana.screens.onboarding.OnboardingScreen
import com.menusemana.screens.plan.MiSemanaScreen
import com.menusemana.screens.profile.PerfilScreen
import com.menusemana.screens.recipes.RecetasScreen
import com.menusemana.screens.recipes.RecipeDetailScreen
import com.menusemana.screens.shopping.ComprasScreen
import com.menusemana.screens.ui.component.MsBottomBar
import com.menusemana.screens.ui.theme.MenuSemanaTheme
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
    val destination = backStack?.destination

    val showBottomBar =
        destination?.let {
            it.hasRoute(Plan::class) ||
                it.hasRoute(Meals::class) ||
                it.hasRoute(Recipes::class) ||
                it.hasRoute(Shopping::class)
        } ?: false

    val selectedIndex =
        when {
            destination?.hasRoute(Plan::class) == true -> 0
            destination?.hasRoute(Meals::class) == true -> 1
            destination?.hasRoute(Recipes::class) == true -> 2
            destination?.hasRoute(Shopping::class) == true -> 3
            else -> 0
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MsBottomBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        val route =
                            when (index) {
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
                    },
                )
            }
        },
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
                    },
                )
            }
            composable<Plan> {
                MiSemanaScreen(
                    contentPadding = innerPadding,
                    onNavigateToMealDetail = { mealId ->
                        navController.navigate(MealDetail(mealId))
                    },
                    onNavigateToProfile = { navController.navigate(Profile) },
                )
            }
            composable<Profile> {
                PerfilScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable<Meals> {
                MisComidasScreen(
                    contentPadding = innerPadding,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(MealDetail(mealId))
                    },
                    onNavigateToAdd = {
                        navController.navigate(AddMeal)
                    },
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
            composable<Recipes> { entry ->
                val importedName by entry.savedStateHandle
                    .getStateFlow("imported_recipe_name", "")
                    .collectAsStateWithLifecycle()
                RecetasScreen(
                    contentPadding = innerPadding,
                    onNavigateToDetail = { mealDbId ->
                        navController.navigate(RecipeDetail(mealDbId))
                    },
                    importedRecipeName = importedName,
                    onImportedConsumed = { entry.savedStateHandle["imported_recipe_name"] = "" },
                )
            }
            composable<RecipeDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<RecipeDetail>()
                RecipeDetailScreen(
                    mealDbId = route.mealDbId,
                    onNavigateUp = { navController.navigateUp() },
                    onImported = { name ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("imported_recipe_name", name)
                        navController.navigateUp()
                    },
                )
            }
            composable<Shopping> {
                ComprasScreen(contentPadding = innerPadding)
            }
        }
    }
}
