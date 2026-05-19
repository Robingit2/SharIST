package com.project.sharist.data.mapper

import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType

fun RideRequest.toEntity() = RideRequestEntity(
    id = id,

    departureLat = departure.latitude,
    departureLng = departure.longitude,
    departureRadiusMeters = departureRadiusMeters,

    arrivalLat = arrival.latitude,
    arrivalLng = arrival.longitude,
    arrivalRadiusMeters = arrivalRadiusMeters,

    desiredDepartureTimeMillis = desiredDepartureTimeMillis,

    toleranceBeforeMinutes = toleranceBeforeMinutes,
    toleranceAfterMinutes = toleranceAfterMinutes,

    passengers = passengers,
    recurringType = recurringType.name
)

fun RideRequestEntity.toDomain() = RideRequest(
    id = id,

    departure = LatLng(departureLat, departureLng),
    departureRadiusMeters = departureRadiusMeters,

    arrival = LatLng(arrivalLat, arrivalLng),
    arrivalRadiusMeters = arrivalRadiusMeters,

    desiredDepartureTimeMillis = desiredDepartureTimeMillis,

    toleranceBeforeMinutes = toleranceBeforeMinutes,
    toleranceAfterMinutes = toleranceAfterMinutes,

    passengers = passengers,
    recurringType = RecurringType.valueOf(recurringType)
)