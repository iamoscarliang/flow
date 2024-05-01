package com.oscarliang.flow.util

import androidx.lifecycle.liveData

abstract class FetchNextSearchPageTask<T> {

    fun asLiveData() = liveData {
        val result = query()
        if (result == null) {
            emit(null)
            return@liveData
        }
        if (!shouldFetch(result)) {
            emit(Resource.success(false))
            return@liveData
        }
        try {
            fetchNextPage(result)
            emit(Resource.success(true))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", true))
        }
    }

    abstract suspend fun query(): T?
    abstract suspend fun fetchNextPage(data: T)
    abstract fun shouldFetch(data: T): Boolean

}