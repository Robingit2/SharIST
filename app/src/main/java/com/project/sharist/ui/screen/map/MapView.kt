package com.project.sharist.ui.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun OpenStreetMapView() {

    val context = LocalContext.current

    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    val fusedLocationClient =
        remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

    var currentPoint by remember {
        mutableStateOf(
            GeoPoint(38.7223, -9.1393)
        )
    }

    LaunchedEffect(permissionState.status.isGranted) {

        if (permissionState.status.isGranted) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        Log.d(
                            "MAP_DEBUG",
                            "LOCATION: ${location.latitude}, ${location.longitude}"
                        )

                        currentPoint = GeoPoint(
                            location.latitude,
                            location.longitude
                        )

                    } else {

                        Log.d(
                            "MAP_DEBUG",
                            "LOCATION IS NULL"
                        )
                    }
                }
        }
    }

    AndroidView(
        factory = {

            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm", 0)
            )

            Configuration.getInstance().userAgentValue =
                context.packageName

            MapView(context).apply {

                setMultiTouchControls(true)

                setTileSource(
                    TileSourceFactory.MAPNIK
                )
            }
        },

        update = { mapView ->

            mapView.controller.setZoom(15.0)

            mapView.controller.setCenter(currentPoint)

            mapView.overlays.clear()

            val marker = Marker(mapView)

            marker.position = currentPoint

            marker.title = "You are here"

            mapView.overlays.add(marker)

            mapView.invalidate()
        }
    )
}