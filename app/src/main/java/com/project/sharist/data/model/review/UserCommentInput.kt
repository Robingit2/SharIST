package com.project.sharist.data.model.review

data class UserCommentInput (
    val raterId: String,
    val targetId: String,
    val comment: String
)