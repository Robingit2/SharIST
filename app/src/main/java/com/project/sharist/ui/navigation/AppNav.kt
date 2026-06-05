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
import com.project.sharist.data.model.getOrNull
import com.project.sharist.data.repository.cachedRideOfferRepository
import com.project.sharist.data.repository.cachedUserRepository
import com.project.sharist.data.repository.sessionRepository
import com.project.sharist.data.usecase.auth.LogoutUserUseCase
import com.project.sharist.data.usecase.user.GetActiveRoleUseCase
import com.project.sharist.data.usecase.user.SetActiveRoleUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.util.Log
import com.project.sharist.ui.screen.favorite.FavoriteViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.project.sharist.data.model.favorite.FavoriteLocationEntity
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userRepository = remember(context) { cachedUserRepository(context) }
    val rideOfferRepository = remember(context) { cachedRideOfferRepository(context) }
    val sessionRepository = remember(context) { sessionRepository(context) }
    val getActiveRoleUseCase = remember(sessionRepository) {
        GetActiveRoleUseCase(sessionRepository)
    }
    val setActiveRoleUseCase = remember(userRepository, sessionRepository) {
        SetActiveRoleUseCase(
            userRepository = userRepository,
            sessionRepository = sessionRepository
        )
    }
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

    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favorites by favoriteViewModel.favorites.collectAsState()

    LaunchedEffect(showDrawer, currentRoute) {
        if (!showDrawer) return@LaunchedEffect

        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
        val roles = userRepository.getUserRoles(currentUserId).getOrNull().orEmpty()
        val savedRole = getActiveRoleUseCase(currentUserId).first()
        val selectedRole = when {
            savedRole in roles -> savedRole
            activeRole in roles -> activeRole
            else -> roles.firstOrNull()
        }

        userRoles = roles
        activeRole = selectedRole
        if (selectedRole != null && selectedRole != savedRole) {
            setActiveRoleUseCase(currentUserId, selectedRole)
        }
    }
    LaunchedEffect(activeRole, supabase.auth.currentUserOrNull()?.id) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
        Log.d("FAV_DEBUG", "userId = $userId")
        favoriteViewModel.loadFavorites(userId)
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
                favoriteViewModel = favoriteViewModel,
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
                    favorites = favorites,
                    //favoriteViewModel = favoriteViewModel,
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
                        val currentUserId = supabase.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            scope.launch {
                                setActiveRoleUseCase(currentUserId, role)
                                activeRole = role
                                drawerState.close()
                                navController.navigate(Screen.Home.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onFavoriteClick = { favorite ->
                        scope.launch { drawerState.close() }
                        favoriteViewModel.selectFavorite(favorite)
                    },
                    onDeleteFavorite = { id ->
                        scope.launch {
                            val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                            favoriteViewModel.removeFavorite(userId,id)
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
    modifier: Modifier = Modifier,
    favoriteViewModel: FavoriteViewModel
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
                    try {
                    Log.d("LOGIN", "Navigating to Home")

                    navController.navigate(Screen.Home.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    Log.e("LOGIN", "Navigation crash", e)
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
                favoriteViewModel = favoriteViewModel,
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

        composable(
            route = "${Screen.RideRequest.route}?pendingRequestId={pendingRequestId}",
            arguments = listOf(navArgument("pendingRequestId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entry ->
            RideRequestScreen(
                pendingRequestId = entry.arguments?.getString("pendingRequestId"),
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
                },
                onEditPendingRequestClick = { requestId ->
                    navController.navigate("${Screen.RideRequest.route}?pendingRequestId=$requestId")
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
    favorites: List<FavoriteLocationEntity>,
    onFavoriteClick: (FavoriteLocationEntity) -> Unit,
    onDeleteFavorite: (Long) -> Unit,

    onLogoutClick: () -> Unit,
    //favoriteViewModel: FavoriteViewModel,
) {
    var favoritesExpanded by rememberSaveable { mutableStateOf(false) }

    ModalDrawerSheet {
        Text(
            text = activeRole?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "Menu",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        DrawerItem("Home", onHomeClick)
        Text("Favorites size: ${favorites.size}")
        DrawerItem("Profile", onProfileClick)
        DrawerItem("Settings", onSettingsClick)
        DrawerItem("History", onHistoryClick)
        DrawerItem(
            if (favoritesExpanded)
                "- Favorite Locations"
            else
                "+ Favorite Locations"
        ) {
            favoritesExpanded = !favoritesExpanded
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        /*if (favoritesExpanded) {
            Column {
                favorites.forEach { favorite ->
                    NavigationDrawerItem(
                        modifier = Modifier.padding(start = 24.dp),
                        label = {
                            Text(favorite.name ?: "Unnamed")
                        },
                        selected = false,
                        onClick = {
                            onFavoriteClick(favorite)
                        }
                    )
                }
            }
        }*/
        if (favoritesExpanded) {
            Column {
                favorites.forEach { favorite ->

                    var menuExpanded by remember { mutableStateOf(false) }

                    NavigationDrawerItem(
                        modifier = Modifier.padding(start = 24.dp),
                        label = {
                            Text(favorite.name ?: "Unnamed")
                        },
                        selected = false,
                        onClick = {
                            onFavoriteClick(favorite)
                        },
                        badge = {
                            Box {
                                Text(
                                    text = "..",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            menuExpanded = true
                                        }
                                )

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            favorite.id?.let { id ->
                                                onDeleteFavorite(id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
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
