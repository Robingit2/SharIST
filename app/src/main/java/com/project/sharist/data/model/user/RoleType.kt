package com.project.sharist.data.model.user

enum class RoleType(val id: String) {
    DRIVER("077e7f9a-1637-47bb-8fb3-ff853a8b5ad4"),
    PASSENGER("c9334bc1-1cf7-4bfa-9a78-f568115a64d1");

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
