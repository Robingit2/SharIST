package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order

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

    suspend fun getCommentsPageByTarget(targetId: String, from: Long, to: Long): UserCommentsPage {
        val result = userCommentsTable.select {
            filter {
                eq("target_user_id", targetId)
            }
            order("created_at", Order.DESCENDING)
            range(from, to)
            count(Count.EXACT)
        }

        return UserCommentsPage(
            comments = result.decodeList<UserComment>(),
            totalCount = result.countOrNull()?.toInt() ?: 0
        )
    }

    suspend fun getCommentsPageByTargetResult(targetId: String, from: Long, to: Long): GenericResult<UserCommentsPage> {
        return safeSupabaseCall {
            getCommentsPageByTarget(targetId, from, to)
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

data class UserCommentsPage(
    val comments: List<UserComment>,
    val totalCount: Int
)

private const val DEFAULT_PAGE_SIZE = 10L
