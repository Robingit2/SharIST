package com.project.sharist.ui.screen.map

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.project.sharist.ui.screen.weather.WeatherOverlay
import com.project.sharist.ui.screen.weather.WeatherViewModel
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import com.project.sharist.ui.screen.favorite.FavoriteViewModel
import com.project.sharist.ui.screen.favorite.FavoriteDisplayMode

import androidx.core.graphics.drawable.DrawableCompat
import com.project.sharist.data.model.favorite.FavoriteLocationEntity
import org.osmdroid.views.overlay.infowindow.InfoWindow
import android.widget.TextView
import android.widget.FrameLayout
import android.graphics.drawable.GradientDrawable

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OpenStreetMapView(
    weatherViewModel: WeatherViewModel,
    favoriteViewModel: FavoriteViewModel,
    centerOnUserLocationTrigger: Int = 0
) {
    val context = LocalContext.current
    val weatherState by weatherViewModel.state.collectAsState()
    // ---------------- PERMISSION ----------------
    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    val hasPermission = permissionState.status.isGranted

    // ---------------- LOCATION CLIENTS ----------------
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val settingsClient = remember {
        LocationServices.getSettingsClient(context)
    }

    // ---------------- LOCATION REQUEST ----------------
    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()
    }

    // ---------------- STATE ----------------
    var currentPoint by remember {
        mutableStateOf(GeoPoint(38.7223, -9.1393)) // Lisbon default
    }

    // ---------------- MAP SETUP ----------------
    val mapView = remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osm", 0)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(18.0)
        }
    }

    val marker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "You"
            icon?.let {
                val drawable = it.constantState?.newDrawable()?.mutate()
                drawable?.setTint(android.graphics.Color.BLUE)
                icon = drawable
            }
            showInfoWindow()
        }
    }
    var weatherLoaded by remember {
        mutableStateOf(false)
    }
    //********************
    val selectedFavorite by favoriteViewModel.selectedLocation.collectAsState()
    val favorites by favoriteViewModel.favorites.collectAsState()
    val displayMode by favoriteViewModel.displayMode.collectAsState()

    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    /*val favoriteMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Favorite"
            isDraggable = true
        }
    }*/
    val favoriteMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Favorite"

            // Get a NEW drawable instance
            val newIcon = this.icon?.constantState?.newDrawable()?.mutate()

            newIcon?.let {
                DrawableCompat.setTint(it, android.graphics.Color.RED)
                icon = it
            }
        }
    }

    favoriteMarker.setOnMarkerDragListener(
        object : Marker.OnMarkerDragListener {

            override fun onMarkerDrag(marker: Marker?) {}
            override fun onMarkerDragStart(marker: Marker?) {}
            override fun onMarkerDragEnd(marker: Marker?) {
                marker?.position?.let {
                    selectedPoint = it
                }
            }
        }
    )

    val longPressOverlay = remember {
        MapEventsOverlay(
            object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(p: GeoPoint?) = false

                override fun longPressHelper(p: GeoPoint?): Boolean {

                    p?.let {
                        selectedPoint = it
                        favoriteMarker.position = it

                        if (!mapView.overlays.contains(favoriteMarker)) {
                            mapView.overlays.add(favoriteMarker)
                        }

                        mapView.invalidate()
                        showDialog = true
                    }

                    return true
                }
            }
        )
    }
    /*LaunchedEffect(selectedFavorite) {
        selectedFavorite?.let { favorite ->
            val lat = favorite.latitude ?: return@let
            val lng = favorite.longitude ?: return@let
            val point = GeoPoint(lat, lng)

            // Center map
            mapView.controller.setCenter(point)
            mapView.controller.setZoom(18.0)

            // Update favorite marker
            favoriteMarker.position = point
            favoriteMarker.title = favorite.name ?: "Favorite"
            if (!mapView.overlays.contains(favoriteMarker)) {
                mapView.overlays.add(favoriteMarker)
            }

            mapView.invalidate()
        }
    }*/
    LaunchedEffect(displayMode, favorites, selectedFavorite) {
        // Remove existing favorite markers only
        mapView.overlays.removeAll {
            it is Marker && it.id?.startsWith("favorite_") == true
        }

        when (displayMode) {

            FavoriteDisplayMode.NONE -> {
                mapView.invalidate()
            }

            FavoriteDisplayMode.SINGLE -> {
                selectedFavorite?.let { favorite ->
                    val lat = favorite.latitude ?: return@let
                    val lng = favorite.longitude ?: return@let

                    val marker = createFavoriteMarker(
                        mapView, favorite)

                    mapView.overlays.add(marker)
                    mapView.controller.setCenter(marker.position)
                }
            }

            FavoriteDisplayMode.ALL -> {
                favorites.forEach { favorite ->
                    val lat = favorite.latitude ?: return@forEach
                    val lng = favorite.longitude ?: return@forEach

                    val marker = createFavoriteMarker(
                        mapView, favorite)

                    mapView.overlays.add(marker)
                    mapView.controller.setCenter(marker.position)
                }
            }
        }

        mapView.invalidate()
    }

    //*************************
    // ---------------- LOCATION CALLBACK ----------------
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->

                    val point = GeoPoint(
                        location.latitude,
                        location.longitude
                    )

                    Log.d("MAP_DEBUG", "Lat: ${location.latitude}, Lng: ${location.longitude}")

                    currentPoint = point
                    if (!weatherLoaded) {

                        weatherLoaded = true

                        weatherViewModel.loadWeather(
                            location.latitude,
                            location.longitude
                        )
                    }
                }
            }
        }
    }


    // ---------------- START LOCATION UPDATES (SAFE) ----------------
    fun startLocationUpdates() {

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) return

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("MAP_DEBUG", "Permission missing", e)
        }
    }

    // ---------------- GPS ENABLE FLOW ----------------
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("MAP_DEBUG", "GPS ENABLED")
            startLocationUpdates()
        }
    }

    // ---------------- CHECK GPS SETTINGS ----------------
    LaunchedEffect(hasPermission) {

        if (!hasPermission) return@LaunchedEffect

        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        settingsClient.checkLocationSettings(request)
            .addOnSuccessListener {
                startLocationUpdates()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    gpsLauncher.launch(
                        IntentSenderRequest.Builder(
                            exception.resolution.intentSender
                        ).build()
                    )
                }
            }
    }

    // ---------------- UPDATE MAP ON LOCATION CHANGE ----------------
    LaunchedEffect(currentPoint) {
        mapView.controller.setCenter(currentPoint)
        marker.position = currentPoint
        mapView.invalidate()
    }

    LaunchedEffect(centerOnUserLocationTrigger) {
        if (centerOnUserLocationTrigger == 0) return@LaunchedEffect

        if (!hasPermission) {
            permissionState.launchPermissionRequest()
            return@LaunchedEffect
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val point = location?.let {
                    GeoPoint(it.latitude, it.longitude)
                } ?: currentPoint

                currentPoint = point
                marker.position = point
                mapView.controller.setZoom(18.0)
                mapView.controller.animateTo(point)
                mapView.invalidate()
            }
        } catch (e: SecurityException) {
            Log.e("MAP_DEBUG", "Permission missing", e)
        }
    }

    // ---------------- ADD MARKER ONCE ----------------
    /*LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(marker)) {
            mapView.overlays.add(marker)
        }
    }*/
    //**************
    LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(marker)) {
            mapView.overlays.add(marker)
        }

        if (!mapView.overlays.contains(longPressOverlay)) {
            mapView.overlays.add(longPressOverlay)
        }
    }

    // ---------------- CLEANUP ----------------
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            mapView.onDetach()
        }
    }

    // ---------------- UI ----------------
    /*AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    )*/
    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .fillMaxWidth(0.70f)
        ) {

            weatherState.weather?.let { weather ->
                WeatherOverlay(
                    weather = weather,
                    forecast = weatherState.forecast
                )
            }
            //************
        }
        if (showDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Save Favorite") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = selectedPoint != null && name.isNotBlank(),
                        onClick = {
                            val userId = supabase.auth.currentUserOrNull()?.id

                            if (userId != null) {
                                selectedPoint?.let { point ->
                                   // Log.d("FAV_INSERT", "Trying insert: name=$name lat=${userId} lng=${point.longitude}")
                                    favoriteViewModel.addFavorite(
                                        userId = userId,
                                        name = name.trim(),
                                        lat = point.latitude,
                                        lng = point.longitude
                                    )
                                }
                            }
                            showDialog = false
                            selectedPoint = null
                            mapView.overlays.remove(favoriteMarker)
                            mapView.invalidate()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            selectedPoint = null

                            mapView.overlays.remove(favoriteMarker)
                            mapView.invalidate()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}
private fun createFavoriteMarker(
    mapView: MapView,
    favorite: FavoriteLocationEntity
): Marker {

    return Marker(mapView).apply {
        id = "favorite_${favorite.id}"

        position = GeoPoint(
            favorite.latitude ?: 0.0,
            favorite.longitude ?: 0.0
        )

        title = favorite.name ?: "Fav"

        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        icon?.let {
            val drawable = it.constantState?.newDrawable()?.mutate()
            drawable?.setTint(android.graphics.Color.RED)
            icon = drawable
        }

        infoWindow = object : InfoWindow(
            FrameLayout(mapView.context),
            mapView
        ) {
            override fun onOpen(item: Any?) {
                val marker = item as Marker

                val container = FrameLayout(mapView.context).apply {
                    setPadding(8, 4, 8, 4)
                }

                val tv = TextView(mapView.context).apply {
                    text = marker.title
                    textSize = 10f
                    setTextColor(android.graphics.Color.BLACK)
                    setPadding(6, 2, 6, 2)
                }

                val bg = GradientDrawable().apply {
                    setColor(android.graphics.Color.WHITE)
                    cornerRadius = 10f
                    setStroke(1, android.graphics.Color.LTGRAY)
                }

                container.background = bg
                container.addView(tv)

                mView = container
            }

            override fun onClose() {}
        }
        showInfoWindow()
    }
}
