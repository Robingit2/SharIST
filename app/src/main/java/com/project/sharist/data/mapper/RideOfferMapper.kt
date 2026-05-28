package com.project.sharist.data.mapper

import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun RideOffer.toEntity() = RideOfferEntity(
    id = id,
    driverId = driverId,

    departureLat = departure.latitude,
    departureLng = departure.longitude,

    arrivalLat = arrival.latitude,
    arrivalLng = arrival.longitude,

    departureTime = departureTimeMillis.toTimestampz(),
    estimatedArrivalTime = estimatedArrivalTimeMillis.toTimestampz(),

    cost = cost,
    vehicleCapacity = vehicleCapacity,

    cancellationWindowMinutes = cancellationWindowMinutes,

    recurringType = recurringType.name
)

fun RideOfferEntity.toDomain() = RideOffer(
    id = id,
    driverId = driverId,

    departure = LatLng(departureLat, departureLng),
    arrival = LatLng(arrivalLat, arrivalLng),

    departureTimeMillis = departureTime.toMillis(),
    estimatedArrivalTimeMillis = estimatedArrivalTime.toMillis(),

    cost = cost,
    vehicleCapacity = vehicleCapacity,

    cancellationWindowMinutes = cancellationWindowMinutes,

    recurringType = RecurringType.valueOf(recurringType)
)

private fun Long.toTimestampz(): String {
    return timestampFormat().format(Date(this)).replace("+0000", "+00:00")
}

private fun String.toMillis(): Long {
    val value = trim()
    require(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}").matches(value)) {
        "Unsupported timestamp format: $this"
    }

    return timestampFormat()
        .parse(value.replaceLastColonInOffset())
        ?.time ?: 0L
}

private fun String.replaceLastColonInOffset(): String {
    return replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
}

private fun timestampFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}
