package com.project.sharist.data.model.helpers

fun isUniqueViolation(e: Throwable): Boolean {
    return e.message?.contains("23505") == true
}