package com.project.sharist.data.mapper

import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType

fun RideOffer.toEntity() = RideOfferEntity(
    id = id,

    departureLat = departure.latitude,
    departureLng = departure.longitude,

    arrivalLat = arrival.latitude,
    arrivalLng = arrival.longitude,

    departureTimeMillis = departureTimeMillis,
    estimatedArrivalTimeMillis = estimatedArrivalTimeMillis,

    cost = cost,
    vehicleCapacity = vehicleCapacity,

    cancellationWindowMinutes = cancellationWindowMinutes,

    recurringType = recurringType.name
)

fun RideOfferEntity.toDomain() = RideOffer(
    id = id,

    departure = LatLng(departureLat, departureLng),
    arrival = LatLng(arrivalLat, arrivalLng),

    departureTimeMillis = departureTimeMillis,
    estimatedArrivalTimeMillis = estimatedArrivalTimeMillis,

    cost = cost,
    vehicleCapacity = vehicleCapacity,

    cancellationWindowMinutes = cancellationWindowMinutes,

    recurringType = RecurringType.valueOf(recurringType)
)