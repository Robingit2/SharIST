package com.project.sharist.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.cachedRideOfferRepository
import com.project.sharist.data.repository.cachedUserRepository
import com.project.sharist.data.usecase.auth.LogoutUserUseCase
import com.project.sharist.supabase
import com.project.sharist.ui.navigation.Navigation.Screen
import com.project.sharist.ui.screen.available_rides.AvailableRidesScreen
import com.project.sharist.ui.screen.history.HistoryScreen
import com.project.sharist.ui.screen.home.HomeScreen
import com.project.sharist.ui.screen.login.LoginScreen
import com.project.sharist.ui.screen.reservations.ReservationsScreen
import com.project.sharist.ui.screen.ride_offer.MyRideOffersScreen
import com.project.sharist.ui.screen.ride_offer.RideOfferScreen
import com.project.sharist.ui.screen.ride_request.MyRideRequestsScreen
import com.project.sharist.ui.screen.ride_request.RideRequestScreen
import com.project.sharist.ui.screen.signup.SignupScreen
import com.project.sharist.ui.screen.users.ProfileScreen
import com.project.sharist.ui.screen.vehicles.MyVehiclesScreen
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userRepository = remember(context) { cachedUserRepository(context) }
    val rideOfferRepository = remember(context) { cachedRideOfferRepository(context) }
    val logoutUserUseCase = remember(context) {
        LogoutUserUseCase(
            userRepository = userRepository,
            rideOfferRepository = rideOfferRepository
        )
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            AppNavHost(
                navController = navController,
                activeRole = activeRole ?: userRoles.firstOrNull() ?: RoleType.PASSENGER,
                onLogout = onLogout,
                modifier = if (showDrawer && currentRoute != Screen.Home.route) {
                    Modifier.padding(top = 60.dp)
                } else {
                    Modifier
                }
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
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                AppDrawerContent(
                    activeRole = activeRole,
                    userRoles = userRoles,
                    onHomeClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    onProfileClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Profile.route)
                    },
                    onSettingsClick = { scope.launch { drawerState.close() } },
                    onHistoryClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.History.route)
                    },
                    onMyVehiclesClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MyVehicles.route)
                    },
                    onMyOffersClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MyRideOffers.route)
                    },
                    onReservationsClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Reservations.route)
                    },
                    onMyRequestsClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MyRideRequests.route)
                    },
                    onAvailableRidesClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.AvailableRides.route)
                    },
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
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
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
            HomeScreen(
                role = activeRole,
                onCreateRideOfferClick = {
                    navController.navigate(Screen.RideOffer.route)
                },
                onCreateRideRequestClick = {
                    navController.navigate(Screen.RideRequest.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSettingsClick = { navController.popBackStack() },
                onLogoutClick = onLogout
            )
        }

        composable(
            route = "${Screen.Profile.route}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { entry ->
            val userId = entry.arguments?.getString("userId")
            ProfileScreen(
                profileUserId = userId,
                currentUserId = supabase.auth.currentUserOrNull()?.id,
                onSettingsClick = { navController.popBackStack() },
                onLogoutClick = onLogout
            )
        }

        composable(Screen.MyVehicles.route) {
            MyVehiclesScreen()
        }

        composable(Screen.History.route) {
            HistoryScreen(
                role = activeRole,
                onUserClick = { userId ->
                    navController.navigate("${Screen.Profile.route}/$userId")
                }
            )
        }

        composable(Screen.RideOffer.route) {
            RideOfferScreen(
                onRideOfferSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RideRequest.route) {
            RideRequestScreen(
                onRideRequestSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MyRideOffers.route) {
            MyRideOffersScreen(
                onPassengerClick = { passengerId ->
                    navController.navigate("${Screen.Profile.route}/$passengerId")
                }
            )
        }

        composable(Screen.MyRideRequests.route) {
            MyRideRequestsScreen(
                onDriverClick = { driverId ->
                    navController.navigate("${Screen.Profile.route}/$driverId")
                }
            )
        }

        composable(Screen.AvailableRides.route) {
            AvailableRidesScreen(
                onDriverClick = { driverId ->
                    navController.navigate("${Screen.Profile.route}/$driverId")
                }
            )
        }

        composable(Screen.Reservations.route) {
            ReservationsScreen(
                onDriverClick = { driverId ->
                    navController.navigate("${Screen.Profile.route}/$driverId")
                }
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
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onMyVehiclesClick: () -> Unit,
    onMyOffersClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
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

        DrawerItem("Home", onHomeClick)
        DrawerItem("Profile", onProfileClick)
        DrawerItem("Settings", onSettingsClick)
        DrawerItem("History", onHistoryClick)

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        when (activeRole) {
            RoleType.DRIVER -> {
                DrawerItem("My vehicles", onMyVehiclesClick)
                DrawerItem("My Ride offers", onMyOffersClick)

                if (RoleType.PASSENGER in userRoles) {
                    DrawerItem("Switch to passenger") {
                        onSwitchRoleClick(RoleType.PASSENGER)
                    }
                }
            }

            RoleType.PASSENGER -> {
                DrawerItem("Reservations", onReservationsClick)
                DrawerItem("My ride requests", onMyRequestsClick)
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
