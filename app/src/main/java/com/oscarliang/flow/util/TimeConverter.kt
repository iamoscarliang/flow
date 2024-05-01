package com.oscarliang.flow.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeConverter {

    fun getTimePassBy(
        hour: Int,
        now: Long = System.currentTimeMillis()
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hourInMillis = hour * 60 * 60 * 1000
        val diff = now - hourInMillis
        return dateFormat.format(Date(diff))
    }

}