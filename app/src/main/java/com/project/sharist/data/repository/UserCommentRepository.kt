package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class UserCommentRepository {

    private val userCommentsTable = supabase.postgrest["user_comments"]

    suspend fun getCommentById(commentId: String) : GenericResult<UserComment?> {
        return safeSupabaseCall {
            userCommentsTable.select {
                filter {
                    eq("id", commentId)
                }
            }.decodeSingleOrNull<UserComment>()
        }
    }

    suspend fun getCommentByUsers(raterId: String, targetId: String) : GenericResult<UserComment?> {
        return safeSupabaseCall {
            userCommentsTable.select {
                filter {
                    and {
                        eq("rater_user_id", raterId)
                        eq("target_user_id", targetId)
                    }
                }
            }.decodeSingleOrNull<UserComment>()
        }
    }

    suspend fun getCommentsByTarget(targetId: String) : GenericResult<List<UserComment>> {
        return safeSupabaseCall {
            userCommentsTable.select {
                filter {
                    eq("target_user_id", targetId)
                }
            }.decodeList<UserComment>()
        }
    }

    suspend fun getCommentsByRater(raterId: String) : GenericResult<List<UserComment>> {
        return safeSupabaseCall {
            userCommentsTable.select {
                filter {
                    eq("rater_user_id", raterId)
                }
            }.decodeList<UserComment>()

        }
    }

    suspend fun upsert(comment: UserComment) : GenericResult<Unit> {
        return safeSupabaseCall {
            userCommentsTable.upsert(comment)
        }
    }

    suspend fun delete(raterId: String, targetId: String) : GenericResult<Unit> {
        return safeSupabaseCall {
            userCommentsTable.delete {
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
