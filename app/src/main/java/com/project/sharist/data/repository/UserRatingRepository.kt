package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

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

    suspend fun getRatingsByTarget(targetId: String) : GenericResult<List<UserRating>> {
        return safeSupabaseCall {
            userRatingsTable.select {
                filter {
                    eq("target_user_id", targetId)
                }
            }.decodeList<UserRating>()
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
