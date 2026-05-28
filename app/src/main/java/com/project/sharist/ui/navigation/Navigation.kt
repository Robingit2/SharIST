package com.project.sharist.ui.navigation

class Navigation {
    sealed class Screen(val route: String) {
        object Login : Screen("login")
        object Signup : Screen("signup")
        object Home : Screen("home")

        object Profile : Screen("profile")
        object MyVehicles : Screen("my_vehicles")
    }
}
