package com.oscarliang.flow.db

import androidx.room.TypeConverter

object NewsTypeConverters {

    @TypeConverter
    @JvmStatic
    fun stringToStringList(string: String?): List<String>? {
        return string?.let { s ->
            s.split(",").map {
                it
            }
        }
    }

    @TypeConverter
    @JvmStatic
    fun stringListToString(strings: List<String>?): String? {
        return strings?.joinToString(",")
    }

}