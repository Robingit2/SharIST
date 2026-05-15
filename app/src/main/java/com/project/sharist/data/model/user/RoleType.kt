package com.project.sharist.data.model.user

enum class RoleType {
    DRIVER,
    PASSENGER;

    companion object {
        fun from(value: String): RoleType? {
            return when (value.lowercase()) {
                "driver" -> RoleType.DRIVER
                "passenger" -> RoleType.PASSENGER
                else -> null
            }
        }
    }
}
