package com.example.languagelegends

import LanguageSelection
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.languagelegends.database.AppDatabase
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.screens.ChatScreen
import com.example.languagelegends.screens.ExercisesScreen
import com.example.languagelegends.screens.PathScreen
import com.example.languagelegends.screens.ProfileScreen
import com.example.languagelegends.ui.theme.LanguageLegendsTheme


class MainActivity : ComponentActivity() {
    private val appDatabase: AppDatabase by lazy {
        DatabaseProvider.getDatabase(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LanguageLegendsTheme {
                val navController: NavHostController = rememberNavController()
                var buttonsTrue by remember { mutableStateOf(true) }
                var apiSelectedLanguage by remember { mutableStateOf("English") }

                Scaffold(
                    topBar = {
                        TopBar(onLanguageSelected = { language ->
                            apiSelectedLanguage = language
                        })
                    },
                    bottomBar = {
                        if (buttonsTrue) {
                            BottomBar(
                                navController = navController,
                                modifier = Modifier
                            )
                        }
                    }) { paddingValues ->
                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        NavHost(
                            navController = navController,
                            userProfileDao = appDatabase.userProfileDao(),
                            onBottomBarVisibilityChanged = { isVisible ->
                                buttonsTrue = isVisible
                            },
                            startDestination = Screen.Profile.route,
                            selectedLanguage = apiSelectedLanguage

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(onLanguageSelected: (String) -> Unit) {
    var showLanguageSelection by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showLanguageSelection = !showLanguageSelection }) {
                    Icon(
                        painter = painterResource(id = R.drawable.flag),
                        contentDescription = stringResource(id = R.string.language_selection)
                    )
                }

                if (showLanguageSelection) {
                    LanguageSelection(onLanguageSelected = { apiSelectedLanguage ->
                        showLanguageSelection = false
                        onLanguageSelected(apiSelectedLanguage)
                    })
                }
            }
        }
    }
}


sealed class Screen(
    val route: String,
    val title: Int? = null,
    val icon: @Composable () -> Painter,

    ) {
    data object Profile : Screen(
        "profile",
        R.string.profile, // Resource ID for the title string
        { painterResource(id = R.drawable.person) }
    )

    data object Chat : Screen(
        "chat",
        R.string.chat, // Resource ID for the title string
        { painterResource(id = R.drawable.smart_toy) }
    )

    data object Path : Screen("path",
        R.string.path, // Resource ID for the title string
        { painterResource(id = R.drawable.map) }
    )
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        Screen.Path,
        Screen.Chat,
        Screen.Profile,
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        screens.forEach { screen ->
            NavigationBarItem(
                label = {
                    screen.title?.let { title ->
                        Text(text = stringResource(id = title))
                    }
                },
                icon = {
                    val iconPainter = screen.icon()
                    Icon(painter = iconPainter, contentDescription = null)
                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedTextColor = Color.Gray,
                    selectedTextColor = Color.Black,
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.Black,
                    indicatorColor = Color.LightGray
                ),
            )
        }
    }
}

@Composable
fun NavHost(
    navController: NavHostController,
    userProfileDao: UserProfileDao,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    startDestination: String,
    selectedLanguage: String
) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Profile.route) {
            onBottomBarVisibilityChanged(true)
            ProfileScreen(userProfileDao, selectedLanguage)
        }
        composable(Screen.Chat.route) {
            onBottomBarVisibilityChanged(true)
            ChatScreen().Chats()
        }
        composable(
            route = "exercises/{exerciseNumber}",
            arguments = listOf(navArgument("exerciseNumber") { type = NavType.IntType })
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getInt("exerciseNumber") ?: 1
            onBottomBarVisibilityChanged(false)
            ExercisesScreen(navController, selectedLanguage)
        }
        composable(Screen.Path.route) {
            onBottomBarVisibilityChanged(true)
            PathScreen(navController = navController, selectedLanguage)
        }
    }
}



