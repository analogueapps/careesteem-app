package com.aits.careesteem.local.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter

@ProvidedTypeConverter
class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String = value?.joinToString(",") ?: ""

    @TypeConverter
    fun toList(value: String?): List<String> = value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
}


