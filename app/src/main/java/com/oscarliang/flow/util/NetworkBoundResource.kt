package com.oscarliang.flow.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map

abstract class NetworkBoundResource<ResultType, RequestType> {

    fun asLiveData() = liveData<Resource<ResultType>> {
        emit(Resource.loading(null))

        if (shouldFetch(query())) {
            try {
                val fetchedData = fetch()
                saveFetchResult(fetchedData)
                emitSource(queryObservable().map { Resource.success(it) })
            } catch (e: Exception) {
                onFetchFailed(e)
                emitSource(queryObservable().map {
                    Resource.error(e.message ?: "Unknown error", it)
                })
            }
        } else {
            emitSource(queryObservable().map { Resource.success(it) })
        }
    }

    abstract suspend fun query(): ResultType?
    abstract fun queryObservable(): LiveData<ResultType>
    abstract suspend fun fetch(): RequestType
    abstract suspend fun saveFetchResult(data: RequestType)
    open fun onFetchFailed(exception: Exception) = Unit
    open fun shouldFetch(data: ResultType?) = true

}