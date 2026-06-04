package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRatingRepository {

    private val userRatingsTable = supabase.postgrest["user_ratings"]

    suspend fun getRatingById(ratingId: String) : GenericResult<UserRating?> {
        return safeSupabaseCall {
            userRatingsTable.select {
                filter {
                    eq("id", ratingId)
                }
            }.decodeSingleOrNull<UserRating>()
        }
    }

    suspend fun getRatingByUsers(raterId: String, targetId: String) : GenericResult<UserRating?> {
        return safeSupabaseCall {
            userRatingsTable.select {
                filter {
                    and {
                        eq("rater_user_id", raterId)
                        eq("target_user_id", targetId)
                    }
                }
            }.decodeSingleOrNull<UserRating>()
        }
    }

    suspend fun getRatingsByRater(raterId: String) : GenericResult<List<UserRating>> {
        return safeSupabaseCall {
            userRatingsTable.select {
                filter {
                    eq("rater_user_id", raterId)
                }
            }.decodeList<UserRating>()

        }
    }

    suspend fun getRatingStatsByTarget(targetId: String): GenericResult<UserRatingStats> {
        return safeSupabaseCall {
            supabase.postgrest["user_review_stats"].select {
                filter {
                    eq("user_id", targetId)
                }
            }.decodeSingle<UserRatingStats>()
        }
    }

    suspend fun upsert(rating: UserRating) : GenericResult<Unit> {
        return safeSupabaseCall {
            userRatingsTable.upsert(rating)
        }
    }

    suspend fun delete(raterId: String, targetId: String) : GenericResult<Unit> {
        return safeSupabaseCall {
            userRatingsTable.delete {
                filter {
                    and {
                        eq("rater_user_id", raterId)
                        eq("target_user_id", targetId)
                    }
                }
            }
        }
    }


}

@Serializable
data class UserRatingStats(
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("average_rating")
    val averageRating: Double = 0.0,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    @SerialName("rating_1_count")
    val rating1Count: Int = 0,
    @SerialName("rating_2_count")
    val rating2Count: Int = 0,
    @SerialName("rating_3_count")
    val rating3Count: Int = 0,
    @SerialName("rating_4_count")
    val rating4Count: Int = 0,
    @SerialName("rating_5_count")
    val rating5Count: Int = 0,
) {
    fun histogram(): Map<Int, Int> {
        return mapOf(
            1 to rating1Count,
            2 to rating2Count,
            3 to rating3Count,
            4 to rating4Count,
            5 to rating5Count
        )
    }
}
