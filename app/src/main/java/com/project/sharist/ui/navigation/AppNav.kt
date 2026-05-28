package com.project.sharist.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.usecase.auth.LogoutUserUseCase
import com.project.sharist.supabase
import com.project.sharist.ui.navigation.Navigation.Screen
import com.project.sharist.ui.screen.home.HomeScreen
import com.project.sharist.ui.screen.login.LoginScreen
import com.project.sharist.ui.screen.signup.SignupScreen
import com.project.sharist.ui.screen.users.ProfileScreen
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val logoutUserUseCase = remember { LogoutUserUseCase() }

    var userRoles by remember { mutableStateOf<List<RoleType>>(emptyList()) }
    var activeRole by remember { mutableStateOf<RoleType?>(null) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showDrawer = currentRoute != null &&
        currentRoute != Screen.Login.route &&
        currentRoute != Screen.Signup.route

    LaunchedEffect(showDrawer, currentRoute) {
        if (!showDrawer) return@LaunchedEffect

        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
        val roles = userRepository.getUserRoles(currentUserId)

        userRoles = roles
        if (activeRole !in roles) {
            activeRole = roles.firstOrNull()
        }
    }

    LaunchedEffect(showDrawer) {
        if (!showDrawer && drawerState.isOpen) {
            drawerState.close()
        }
    }

    val onLogout: () -> Unit = {
        scope.launch {
            drawerState.close()
            logoutUserUseCase()
            userRoles = emptyList()
            activeRole = null
            navController.navigate(Screen.Login.route) {
                clearBackStack()
            }
        }
    }

    val appContent: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                activeRole = activeRole ?: userRoles.firstOrNull() ?: RoleType.PASSENGER,
                onLogout = onLogout
            )

            if (showDrawer) {
                FloatingActionButton(
                    onClick = {
                        scope.launch { drawerState.open() }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Open navigation menu")
                }
            }
        }
    }

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    activeRole = activeRole,
                    userRoles = userRoles,
                    onProfileClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Profile.route)
                    },
                    onSettingsClick = { scope.launch { drawerState.close() } },
                    onHistoryClick = { scope.launch { drawerState.close() } },
                    onMyVehiclesClick = { scope.launch { drawerState.close() } },
                    onMyOffersClick = { scope.launch { drawerState.close() } },
                    onReservationsClick = { scope.launch { drawerState.close() } },
                    onAvailableRidesClick = { scope.launch { drawerState.close() } },
                    onSwitchRoleClick = { role ->
                        activeRole = role
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    onLogoutClick = onLogout
                )
            },
            content = appContent
        )
    } else {
        appContent()
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    activeRole: RoleType,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignupClick = {
                    navController.navigate(Screen.Signup.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onLoginClick = {
                    navController.popBackStack()
                },
                onSignupComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(role = activeRole)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = { navController.popBackStack() },
                onSettingsClick = { navController.popBackStack() },
                onLogoutClick = onLogout
            )
        }
    }
}

private fun NavOptionsBuilder.clearBackStack() {
    popUpTo(Screen.Home.route) {
        inclusive = true
    }
    launchSingleTop = true
}

@Composable
private fun AppDrawerContent(
    activeRole: RoleType?,
    userRoles: List<RoleType>,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onMyVehiclesClick: () -> Unit,
    onMyOffersClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onAvailableRidesClick: () -> Unit,
    onSwitchRoleClick: (RoleType) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            text = activeRole?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "Menu",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        DrawerItem("Profile", onProfileClick)
        DrawerItem("Settings", onSettingsClick)
        DrawerItem("History", onHistoryClick)

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        when (activeRole) {
            RoleType.DRIVER -> {
                DrawerItem("My vehicles", onMyVehiclesClick)
                DrawerItem("My offers", onMyOffersClick)

                if (RoleType.PASSENGER in userRoles) {
                    DrawerItem("Switch to passenger") {
                        onSwitchRoleClick(RoleType.PASSENGER)
                    }
                }
            }

            RoleType.PASSENGER -> {
                DrawerItem("Reservations", onReservationsClick)
                DrawerItem("Available Rides", onAvailableRidesClick)

                if (RoleType.DRIVER in userRoles) {
                    DrawerItem("Switch to driver") {
                        onSwitchRoleClick(RoleType.DRIVER)
                    }
                }
            }

            null -> Unit
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem("Logout", onLogoutClick)
    }
}

@Composable
private fun DrawerItem(
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick
    )
}
