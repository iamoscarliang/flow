package com.oscarliang.flow.util

import androidx.collection.ArrayMap
import java.util.concurrent.TimeUnit

class RateLimiter<KEY>(timeout: Int, timeUnit: TimeUnit) {

    private val timestamps = ArrayMap<KEY, Long>()
    private val timeout = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shouldFetch(
        key: KEY,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val lastFetched = timestamps[key]
        if (lastFetched == null) {
            timestamps[key] = now
            return true
        }
        if (now - lastFetched >= timeout) {
            timestamps[key] = now
            return true
        }
        return false
    }

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }

}