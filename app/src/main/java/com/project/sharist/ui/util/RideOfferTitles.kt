package com.project.sharist.ui.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RideOffer
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun buildRideOfferTitles(
    context: Context,
    offers: List<RideOffer>
): Map<String, String> {
    return withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        offers.associate { offer ->
            val departure = geocoder.addressFor(offer.departure) ?: offer.departure.formatCoordinates()
            val arrival = geocoder.addressFor(offer.arrival) ?: offer.arrival.formatCoordinates()
            offer.id to "$departure -> $arrival"
        }
    }
}

private fun Geocoder.addressFor(location: LatLng): String? {
    return try {
        getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()
            ?.toShortAddress()
    } catch (_: Exception) {
        null
    }
}

private fun Address.toShortAddress(): String? {
    val street = listOfNotNull(thoroughfare, subThoroughfare)
        .joinToString(" ")
        .ifBlank { null }
    val area = listOfNotNull(locality, subAdminArea)
        .distinct()
        .firstOrNull()

    return listOfNotNull(street, area)
        .joinToString(", ")
        .ifBlank { getAddressLine(0) }
}

private fun LatLng.formatCoordinates(): String {
    return "%.5f, %.5f".format(Locale.US, latitude, longitude)
}
