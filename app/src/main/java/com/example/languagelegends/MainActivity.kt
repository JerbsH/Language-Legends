package com.example.languagelegends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
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
import com.example.languagelegends.features.LanguageSelection
import com.example.languagelegends.features.UserProfileViewModel
import com.example.languagelegends.features.icon
import com.example.languagelegends.screens.ChatScreen
import com.example.languagelegends.screens.ExercisesScreen
import com.example.languagelegends.screens.PathScreen
import com.example.languagelegends.screens.ProfileScreen
import com.example.languagelegends.ui.theme.LanguageLegendsTheme
import com.murgupluoglu.flagkit.FlagKit


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
                val application = this.application
                val userProfileViewModel = remember { UserProfileViewModel(application) }
                var isNameScreenActive by remember { mutableStateOf(false) }

                userProfileViewModel.loadSelectedLanguage()

                Scaffold(
                    topBar = {
                        if (!isNameScreenActive) {
                            TopBar(userProfileViewModel)
                        }
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
                                isNameScreenActive = !isVisible
                            },
                            startDestination = Screen.Profile.route,
                            selectedLanguage = apiSelectedLanguage, userProfileViewModel

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(userProfileViewModel: UserProfileViewModel) {
    var showLanguageSelection by remember { mutableStateOf(false) }

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
                val selectedLanguageIcon = userProfileViewModel.selectedLanguageIcon
                val flagResId = FlagKit.getResId(LocalContext.current, selectedLanguageIcon)
                Image(
                    painter = painterResource(flagResId),
                    contentDescription = "Flag of selected language",
                    modifier = Modifier.size(36.dp)
                )
            }

            if (showLanguageSelection) {
                LanguageSelection(
                    userProfileViewModel = userProfileViewModel,
                    onLanguageSelected = { apiSelectedLanguage ->
                        // Close the language selection menu
                        showLanguageSelection = false
                        userProfileViewModel.updateLanguage(apiSelectedLanguage)
                        userProfileViewModel.selectedLanguageIcon = icon(apiSelectedLanguage)
                    }
                )
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
    selectedLanguage: String,
    userProfileViewModel: UserProfileViewModel

) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Profile.route) {
            onBottomBarVisibilityChanged(true)
            ProfileScreen(
                userProfileDao,
                selectedLanguage,
                onBottomBarVisibilityChanged,
                userProfileViewModel
            )
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



