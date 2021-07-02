package com.mthaler.ordertaking

object ConstrainedType {

    /// Create a constrained string using the constructor provided
    /// Return Error if input is null, empty, or length > maxLen
    fun <T>createString(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): Result<T> {
        if (str == null || str.isEmpty()) {
            val msg = "$fieldName must not be null or empty"
            return Result.Error(msg)
        } else if (str.length > maxLen) {
            val msg =  "$fieldName must not be more than $maxLen chars"
            return Result.Error(msg)
        } else {
            return Result.Ok(ctor(str))
        }
    }
}