package com.oscarliang.flow.db

import androidx.room.TypeConverter

object NewsTypeConverters {

    @TypeConverter
    @JvmStatic
    fun stringToStringList(data: String?): List<String>? {
        return data?.let {
            it.split(",").map {
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