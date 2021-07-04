package com.mthaler.ordertaking

sealed class Result<out T, out U> {

    data class Ok<T>(val value: T): Result<T, Nothing>()

    data class Error<U>(val value: U): Result<Nothing, U>()

    fun <V>map(f: (T) -> V): Result<V, U> {
        return when(this) {
            is Ok -> Ok(f(value))
            is Error -> this
        }
    }

    fun <V>mapError(f: (U) -> V): Result<T, V> {
        return when(this) {
            is Ok -> this
            is Error -> Error(f(value))
        }
    }

    fun foreach(f: (T) -> Unit) {
        map { f(it) }
    }
}