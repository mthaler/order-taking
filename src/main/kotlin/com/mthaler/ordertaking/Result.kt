package com.mthaler.ordertaking

sealed class Result<out T, out U> {

    data class Ok<T>(val value: T): Result<T, Nothing>()
    data class Error<U>(val value: U): Result<Nothing, U>()
}