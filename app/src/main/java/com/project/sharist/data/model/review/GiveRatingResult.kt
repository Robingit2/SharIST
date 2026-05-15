package com.project.sharist.data.model.review

sealed class GiveRatingResult {
    data object Success : GiveRatingResult()
    data object AlreadyExists : GiveRatingResult()
    data class Failure(val message: String?) : GiveRatingResult()
}