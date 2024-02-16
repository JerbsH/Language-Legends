package com.example.languagelegends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.languagelegends.database.AppDatabase
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.screens.ChatScreen
import com.example.languagelegends.screens.PathScreen
import com.example.languagelegends.screens.ProfileScreen
import com.example.languagelegends.ui.theme.LanguageLegendsTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.languagelegends.screens.ExercisesScreen


class MainActivity : ComponentActivity() {

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "language_legends_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val userProfileDao: UserProfileDao by lazy {
        appDatabase.userProfileDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageLegendsTheme {
                val navController: NavHostController = rememberNavController()
                var buttonsTrue by remember { mutableStateOf(true) }
                Scaffold(
                    topBar = {
                        TopBar()
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
                            userProfileDao = userProfileDao,
                            onBottomBarVisibilityChanged = { isVisible ->
                                buttonsTrue = isVisible
                            },
                            startDestination = Screen.Profile.route
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TopBar() {
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
                Icon(
                    painter = painterResource(id = R.drawable.flag),
                    contentDescription = "Flags",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(36.dp) // Adjust the size as needed
                )
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add",
                    modifier = Modifier.size(36.dp) // Adjust the size as needed

                )
            }
        }
    }
}

    sealed class Screen(
        val route: String,
        val title: String? = null,
        val icon: @Composable () -> Painter,

        ) {
        data object Profile : Screen(
            "profile",
            title = "Profile",
            { painterResource(id = R.drawable.person) }
      )
        data object Chat : Screen(
            "chat",
            title = "Chat",
            { painterResource(id = R.drawable.smart_toy) }
        )
        data object Path : Screen("path",
            title = "Path",
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
                            Text(text = screen.title!!)
                        },
                        icon = {
                            val iconPainter = screen.icon()
                            Icon(painter = iconPainter, contentDescription = null)
                            //Icon(imageVector = screen.icon, contentDescription = "")
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
    startDestination: String
) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Profile.route) {
            onBottomBarVisibilityChanged(true)
            ProfileScreen(userProfileDao)
        }
        composable(Screen.Chat.route) {
            onBottomBarVisibilityChanged(true)
            ChatScreen()
        }
        composable(
            route = "exercises/{exerciseNumber}",
            arguments = listOf(navArgument("exerciseNumber") { type = NavType.IntType })
        ) { navBackStackEntry ->
            val exerciseNumber = navBackStackEntry.arguments?.getInt("exerciseNumber") ?: 1
            onBottomBarVisibilityChanged(false)
            ExercisesScreen(navController)
        }
        composable(Screen.Path.route) {
            onBottomBarVisibilityChanged(true)
            PathScreen(navController = navController)
        }
    }
}



