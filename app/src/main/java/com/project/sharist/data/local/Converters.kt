package com.project.sharist.data.local

import androidx.room.TypeConverter
import com.project.sharist.domain.model.RecurringType

class Converters {

    @TypeConverter
    fun fromRecurring(type: RecurringType): String = type.name

    @TypeConverter
    fun toRecurring(value: String): RecurringType =
        RecurringType.valueOf(value)
}