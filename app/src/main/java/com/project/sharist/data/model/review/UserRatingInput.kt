package com.project.sharist.data.model.review

data class UserRatingInput (
    val raterId: String,
    val targetId: String,
    val rating: Int
)
