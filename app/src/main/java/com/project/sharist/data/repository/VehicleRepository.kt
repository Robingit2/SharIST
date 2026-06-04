package com.project.sharist.data.repository

import android.content.Context
import android.net.Uri
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.ktor.http.ContentType

class VehicleRepository {

    val vehiclesTable = supabase.postgrest["vehicles"]

    suspend fun getVehicleById(vehicleId: String) : GenericResult<Vehicle> {
        return safeSupabaseCall {
            vehiclesTable.select {
                filter {
                    eq("id", vehicleId)
                }
            }.decodeSingle()
        }
    }

    suspend fun getVehiclesByUser(userId: String) : GenericResult<List<Vehicle>> {
        return safeSupabaseCall {
            vehiclesTable.select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList()
        }
    }

    suspend fun insert(vehicle: Vehicle): GenericResult<Unit> {
        return safeSupabaseCall {
            vehiclesTable.upsert(vehicle)
        }
    }

    suspend fun update(vehicleId: String, updates: Map<String, Any>): GenericResult<Unit> {
        return safeSupabaseCall {
            vehiclesTable.update(updates) {
                filter {
                    eq("id", vehicleId)
                }
            }
        }
    }

    suspend fun delete(vehicleId: String): GenericResult<Unit> {
        return safeSupabaseCall {
            vehiclesTable.delete {
                filter {
                    eq("id", vehicleId)
                }
            }
        }
    }

    suspend fun uploadVehiclePhoto(context: Context, userId: String, vehicleId: String, uri: Uri): GenericResult<String> {
        return safeSupabaseCall {
            val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val path = "users/$userId/$vehicleId.${contentType.toVehiclePhotoExtension()}"
            supabase.storage.from("vehicles").upload(path, uri) {
                upsert = true
                this.contentType = ContentType.parse(contentType)
            }
            path
        }
    }

    suspend fun downloadVehiclePhoto(path: String): ByteArray {
        return supabase.storage.from("vehicles").downloadAuthenticated(path)
    }
}

private fun String.toVehiclePhotoExtension(): String {
    return when (lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }
}
