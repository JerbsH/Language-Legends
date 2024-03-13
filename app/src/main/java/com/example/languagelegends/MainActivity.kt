package com.example.languagelegends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.languagelegends.aicomponents.AiChatViewModel
import com.example.languagelegends.database.AppDatabase
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.features.TranslateAPI
import com.example.languagelegends.features.UserProfileViewModel
import com.example.languagelegends.features.icon
import com.example.languagelegends.screens.ChatScreen
import com.example.languagelegends.screens.ExercisesScreen
import com.example.languagelegends.screens.PathScreen
import com.example.languagelegends.screens.ProfileScreen
import com.example.languagelegends.screens.ViewState
import com.example.languagelegends.ui.theme.LanguageLegendsTheme
import com.murgupluoglu.flagkit.FlagKit

/**
 * This is the main activity of the application. It sets up the navigation controller,
 * the bottom navigation bar, and the top bar. It also observes the selected language
 * from the UserProfileViewModel and updates the UI accordingly.
 */
class MainActivity : ComponentActivity() {
    private val appDatabase: AppDatabase by lazy {
        DatabaseProvider.getDatabase(applicationContext)
    }
    private val viewState: ViewState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LanguageLegendsTheme {
                val navController: NavHostController = rememberNavController()
                var buttonsTrue by remember { mutableStateOf(true) }
                var isNameScreenActive by remember { mutableStateOf(false) }
                val userProfileViewModel: UserProfileViewModel = viewModel()
                var apiSelectedLanguage by remember { mutableStateOf(userProfileViewModel.selectedLanguage) }
                val aiChatViewModel = AiChatViewModel(application, userProfileViewModel)

                userProfileViewModel.selectedLanguageLiveData.observe(this@MainActivity) { newLanguage ->
                    apiSelectedLanguage = newLanguage
                }

                userProfileViewModel.loadSelectedLanguage()


                Scaffold(
                    topBar = {
                        if (!isNameScreenActive) {
                            TopBar(userProfileViewModel, aiChatViewModel, viewState)
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
                            selectedLanguage = apiSelectedLanguage,
                            userProfileViewModel,
                            aiChatViewModel,
                            viewState = viewState,
                            apiSelectedLanguage = apiSelectedLanguage
                        )
                    }
                }
            }
        }
    }


    /**
     * This function displays the top bar of the application. It includes a button to toggle
     * the language selection menu and an icon button to show the flag of the currently selected language.
     */
    @Composable
    fun TopBar(userProfileViewModel: UserProfileViewModel, aiChatViewModel: AiChatViewModel, viewState: ViewState) {
        var showLanguageSelection by remember { mutableStateOf(false) }
        val buttonVisible by aiChatViewModel.chatVisible.observeAsState(false)

        fun toggle() {
            aiChatViewModel.isFreeChat.value = false
            aiChatViewModel.menuVisibility.value = true
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (buttonVisible) {
                Button(onClick = {
                    toggle()
                }) {
                    Text(text = stringResource(id = R.string.backButton))
                }
            }
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
                    userProfileViewModel.LanguageSelection(
                        onLanguageSelected = { apiSelectedLanguage ->
                            showLanguageSelection = false
                            userProfileViewModel.updateLanguage(apiSelectedLanguage, viewState)
                            userProfileViewModel.selectedLanguageIcon = icon(apiSelectedLanguage)
                        }
                    )
                }
            }
        }
    }

    /**
     * This sealed class represents the different screens in the application.
     * Each screen has a route,
     * a title, and an icon.
     */
    sealed class Screen(
        val route: String,
        val title: Int? = null,
        val icon: @Composable () -> Painter,

        ) {
        data object Profile : Screen(
            "profile",
            R.string.profile,
            { painterResource(id = R.drawable.person) }
        )

        data object Chat : Screen(
            "chat",
            R.string.chat,
            { painterResource(id = R.drawable.smart_toy) }
        )

        data object Path : Screen("path",
            R.string.path,
            { painterResource(id = R.drawable.map) }
        )
    }

    /**
     * This function displays the bottom navigation bar of the application. It includes navigation
     * items for each screen in the application.
     * When a navigation item is clicked, it navigates
     * to the corresponding screen.
     */
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
                    }
                )
            }
        }
    }

    /**
     * This function sets up the navigation host for the application. It defines the composable
     * functions for each screen in the application and handles navigation between screens.
     */
    @Composable
    fun NavHost(
        navController: NavHostController,
        userProfileDao: UserProfileDao,
        onBottomBarVisibilityChanged: (Boolean) -> Unit,
        startDestination: String,
        selectedLanguage: String,
        userProfileViewModel: UserProfileViewModel,
        aiChatViewModel: AiChatViewModel,
        apiSelectedLanguage: String,
        viewState: ViewState
    ) {
        val translateAPI = TranslateAPI(LocalContext.current)

        var completedExercises by remember { mutableStateOf(viewState.completedExercises) }


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
                    userProfileViewModel,
                    aiChatViewModel
                )
            }
            composable(Screen.Chat.route) {
                onBottomBarVisibilityChanged(true)
                ChatScreen().Chats(aiChatViewModel)
            }
            composable(
                route = "exercises/{exerciseNumber}",
                arguments = listOf(navArgument("exerciseNumber") { type = NavType.IntType })
            ) { navBackStackEntry ->
                navBackStackEntry.arguments?.getInt("exerciseNumber") ?: 1
                onBottomBarVisibilityChanged(false)

                ExercisesScreen(
                    navController,
                    userProfileViewModel,
                    selectedLanguage,
                    aiChatViewModel,
                    translateAPI = translateAPI,
                    onCompleteExercise = {
                        completedExercises++
                        viewState.completeExercise()
                    },
                    viewState
                )
            }
            composable(Screen.Path.route) {
                onBottomBarVisibilityChanged(true)
                PathScreen(
                    navController = navController,
                    userProfileViewModel,
                    selectedLanguage,
                    aiChatViewModel,
                    completedExercises,
                    onCompleteExercise = {
                        viewState.completeExercise()
                    },
                    viewState
                )
            }
        }
    }
}
