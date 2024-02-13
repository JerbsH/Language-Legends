package com.example.languagelegends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.languagelegends.screens.ChatScreen
import com.example.languagelegends.screens.PathScreen
import com.example.languagelegends.screens.ProfileScreen
import com.example.languagelegends.ui.theme.LanguageLegendsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageLegendsTheme {
                val navController: NavHostController = rememberNavController()
                var buttonsTrue by remember { mutableStateOf(true) }
                Scaffold(
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
                        NavHost(navController = navController) { isVisible ->
                            buttonsTrue = isVisible
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector,

    ) {
    object Profile : Screen(
        "profile",
        title = "Profile",
        Icons.Filled.Person
    )

    object Chat : Screen(
        "chat",
        title = "Chat",
        Icons.Filled.Face
    )

    object Path : Screen(
        "path",
        title = "Path",
        Icons.Filled.Home
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
                    Icon(imageVector = screen.icon, contentDescription = "")
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
fun NavHost(navController: NavHostController, onBottomBarVisibilityChanged: (Boolean) -> Unit) {
    NavHost(navController, startDestination = Screen.Profile.route) {
        composable(Screen.Profile.route) {
            onBottomBarVisibilityChanged(true)
            ProfileScreen()
        }
        composable(Screen.Chat.route) {
            onBottomBarVisibilityChanged(true)
            ChatScreen().Chats()
        }
        composable(Screen.Path.route) {
            onBottomBarVisibilityChanged(true)
            PathScreen()
        }
    }
}



