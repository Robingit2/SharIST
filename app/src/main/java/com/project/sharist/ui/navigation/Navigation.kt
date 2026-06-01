package com.project.sharist.ui.navigation

class Navigation {
    sealed class Screen(val route: String) {
        object Login : Screen("login")
        object Signup : Screen("signup")
        object Home : Screen("home")

        object Profile : Screen("profile")
        object MyVehicles : Screen("my_vehicles")
        object RideOffer : Screen("ride_offer")
        object MyRideOffers : Screen("my_ride_offers")
        object RideRequest : Screen("ride_request")
        object AvailableRides : Screen("available_rides")
        object Reservations : Screen("reservations")
    }
}
