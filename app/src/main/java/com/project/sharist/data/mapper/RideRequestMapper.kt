package com.project.sharist.data.mapper

import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun RideRequest.toEntity() = RideRequestEntity(
    id = id,
    passengerId = passengerId,

    departureLat = departure.latitude,
    departureLng = departure.longitude,
    departureRadiusMeters = departureRadiusMeters,

    arrivalLat = arrival.latitude,
    arrivalLng = arrival.longitude,
    arrivalRadiusMeters = arrivalRadiusMeters,

    desiredDepartureTime = desiredDepartureTimeMillis.toTimestampz(),
    departureToleranceMinutes = departureToleranceMinutes,

    recurringType = recurringType.name,
    createdAt = createdAt ?: System.currentTimeMillis().toTimestampz()
)

fun RideRequestEntity.toDomain() = RideRequest(
    id = id,
    passengerId = passengerId,

    departure = LatLng(departureLat, departureLng),
    departureRadiusMeters = departureRadiusMeters,

    arrival = LatLng(arrivalLat, arrivalLng),
    arrivalRadiusMeters = arrivalRadiusMeters,

    desiredDepartureTimeMillis = desiredDepartureTime.toMillis(),
    departureToleranceMinutes = departureToleranceMinutes,

    recurringType = RecurringType.valueOf(recurringType),
    createdAt = createdAt
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
