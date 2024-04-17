package com.oscarliang.flow.util

data class Resource<out T>(
    val state: State,
    val data: T?,
    val message: String?
) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(State.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(State.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(State.LOADING, data, null)
        }
    }

}

enum class State {
    SUCCESS,
    ERROR,
    LOADING
}