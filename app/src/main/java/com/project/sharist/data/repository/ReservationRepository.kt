package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.ReservationEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class ReservationRepository {

    private val reservationsTable = supabase.postgrest["reservations"]

    suspend fun insert(reservation: ReservationEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            reservationsTable.insert(reservation)
        }
    }

    suspend fun getReservationsByPassenger(passengerId: String): List<ReservationEntity> {
        return reservationsTable.select {
            filter {
                eq("passenger_id", passengerId)
            }
        }.decodeList()
    }

    suspend fun getReservations(): List<ReservationEntity> {
        return reservationsTable.select().decodeList()
    }
}
