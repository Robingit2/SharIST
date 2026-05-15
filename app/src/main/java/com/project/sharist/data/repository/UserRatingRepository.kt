package com.project.sharist.data.repository

import com.project.sharist.data.model.review.UserRating
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class UserRatingRepository {

    private val userRatingsTable = supabase.postgrest["user_ratings"]

    suspend fun getRatingById(ratingId: String) : UserRating? {
        return userRatingsTable.select {
            filter {
                eq("id", ratingId)
            }
        }.decodeSingleOrNull<UserRating>()
    }

    suspend fun getRatingByUsers(raterId: String, targetId: String) : UserRating? {
        return userRatingsTable.select {
            filter {
                and {
                    eq("rater_user_id", raterId)
                    eq("target_user_id", targetId)
                }
            }
        }.decodeSingleOrNull<UserRating>()
    }

    suspend fun getRatingsByTarget(targetId: String) : List<UserRating> {
        return userRatingsTable.select {
            filter {
                eq("target_user_id", targetId)
            }
        }.decodeList<UserRating>()
    }

    suspend fun getRatingsByRater(raterId: String) : List<UserRating> {
        return userRatingsTable.select {
            filter {
                eq("rater_user_id", raterId)
            }
        }.decodeList<UserRating>()
    }

    suspend fun insert(rating: UserRating) {
        userRatingsTable.insert(rating)
    }

    suspend fun update(ratingId: String, updates: Map<String, Any>) {
        userRatingsTable.update(updates) {
            filter {
                eq("id", ratingId)
            }
        }
    }

    suspend fun delete(ratingId: String) {
        userRatingsTable.delete {
            filter {
                eq("id", ratingId)
            }
        }
    }


}
