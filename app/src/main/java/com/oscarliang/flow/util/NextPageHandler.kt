package com.oscarliang.flow.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.flow.repository.NewsRepository

class NextPageHandler(
    private val repository: NewsRepository
) : Observer<Resource<Boolean>?> {

    val loadMoreState = MutableLiveData<LoadMoreState>()
    private var nextPageLiveData: LiveData<Resource<Boolean>?>? = null
    private var query: String? = null
    private var hasMore: Boolean = false

    init {
        reset()
    }

    fun queryNextPage(
        query: String,
        date: String,
        count: Int
    ) {
        if (this.query == query) {
            return
        }
        unregister()
        this.query = query
        nextPageLiveData = repository.searchNextPage(query, date, count)
        loadMoreState.value = LoadMoreState(
            isRunning = true,
            hasMore = true,
            errorMessage = null
        )
        nextPageLiveData?.observeForever(this)
    }

    override fun onChanged(value: Resource<Boolean>?) {
        if (value == null) {
            reset()
        } else {
            when (value.state) {
                State.SUCCESS -> {
                    hasMore = value.data == true
                    unregister()
                    loadMoreState.setValue(
                        LoadMoreState(
                            isRunning = false,
                            hasMore = hasMore,
                            errorMessage = null
                        )
                    )
                }

                State.ERROR -> {
                    hasMore = true
                    unregister()
                    loadMoreState.setValue(
                        LoadMoreState(
                            isRunning = false,
                            hasMore = hasMore,
                            errorMessage = value.message
                        )
                    )
                }

                State.LOADING -> {
                    // ignore
                }
            }
        }
    }

    private fun unregister() {
        nextPageLiveData?.removeObserver(this)
        nextPageLiveData = null
        if (hasMore) {
            query = null
        }
    }

    fun reset() {
        unregister()
        hasMore = true
        loadMoreState.value = LoadMoreState(
            isRunning = false,
            hasMore = true,
            errorMessage = null
        )
    }

}

class LoadMoreState(
    val isRunning: Boolean,
    val hasMore: Boolean,
    val errorMessage: String?
) {

    private var handledError = false

    val errorMessageIfNotHandled: String?
        get() {
            if (handledError) {
                return null
            }
            handledError = true
            return errorMessage
        }

}