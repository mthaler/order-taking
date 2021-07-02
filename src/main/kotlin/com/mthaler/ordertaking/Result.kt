package com.mthaler.ordertaking

sealed class Result<out T> {

    data class Ok<T>(val value: T): Result<T>()
    data class Error(val value: String): Result<Nothing>()
}