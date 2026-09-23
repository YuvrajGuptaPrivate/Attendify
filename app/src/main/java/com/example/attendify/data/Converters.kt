package com.example.attendify.data


import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? =
        value?.joinToString(",")

    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? =
        value?.takeIf { it.isNotEmpty() }?.split(",")?.map { it.toFloat() }?.toFloatArray()
}