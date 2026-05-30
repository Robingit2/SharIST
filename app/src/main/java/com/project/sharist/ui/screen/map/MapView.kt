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
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OpenStreetMapView(
    weatherViewModel: WeatherViewModel
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
            title = "You are here"
        }
    }
    var weatherLoaded by remember {
        mutableStateOf(false)
    }
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

    // ---------------- ADD MARKER ONCE ----------------
    LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(marker)) {
            mapView.overlays.add(marker)
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
                .fillMaxWidth(0.85f)
        ) {

            weatherState.weather?.let { weather ->
                WeatherOverlay(
                    weather = weather,
                    forecast = weatherState.forecast
                )
            }
        }
    }
}