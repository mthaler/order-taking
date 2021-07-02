package com.mthaler.ordertaking

sealed class Option<out T> {

    data class Some<T>(val value: T): Option<T>()
    object None: Option<Nothing>()
}