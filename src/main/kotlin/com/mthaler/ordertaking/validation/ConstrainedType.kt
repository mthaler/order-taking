package com.mthaler.ordertaking.validation

import com.mthaler.ordertaking.Option
import com.mthaler.ordertaking.Result

/// Create a constrained string using the constructor provided
/// Return Error if input is null, empty, or length > maxLen
fun <T>createString(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): Result<T> {
    return when {
        str.isEmpty() -> {
            val msg = "$fieldName must not be empty"
            Result.Error(msg)
        }
        str.length > maxLen -> {
            val msg =  "$fieldName must not be more than $maxLen chars"
            Result.Error(msg)
        }
        else -> {
            Result.Ok(ctor(str))
        }
    }
}

/// Create a optional constrained string using the constructor provided
/// Return None if input is null, empty.
/// Return error if length > maxLen
/// Return Some if the input is valid
fun <T>createStringOption(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): Result<Option<T>> {
    return when {
        str.isEmpty() -> {
            Result.Ok(Option.None)
        }
        str.length > maxLen -> {
            val msg =  "$fieldName must not be more than $maxLen chars"
            Result.Error(msg)
        }
        else -> {
            Result.Ok(Option.Some(ctor(str)))
        }
    }
}

/// Create a constrained integer using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createInt(fieldName: String, ctor: (Int) -> T, minVal: Int,  maxVal: Int, i: Int): Result<T> {
    return when {
        i < minVal -> {
            val msg = "$fieldName: Must not be less than $minVal"
            Result.Error(msg)
        }
        i > maxVal -> {
            val msg = "$fieldName: Must not be greater than $maxVal"
            Result.Error(msg)
        }
        else -> {
            Result.Ok(ctor(i))
        }
    }
}

/// Create a constrained decimal using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createDecimal(fieldName: String, ctor: (Double) -> T, minVal: Double,  maxVal: Double, i: Double): Result<T> {
    return when {
        i < minVal -> {
            val msg = "$fieldName: Must not be less than $minVal"
            Result.Error(msg)
        }
        i > maxVal -> {
            val msg = "$fieldName: Must not be greater than $maxVal"
            Result.Error(msg)
        }
        else -> {
            Result.Ok(ctor(i))
        }
    }
}

/// Create a constrained string using the constructor provided
/// Return Error if input is null. empty, or does not match the regex pattern
fun <T>createLike(fieldName: String, ctor: (String) -> T, pattern: String, str: String): Result<T> {
    return when {
        str.isEmpty() -> {
            val msg = "$fieldName must not be null or empty"
            Result.Error(msg)
        }
        pattern.toRegex().matches(str) -> {
            Result.Ok(ctor(str))
        }
        else -> {
            val msg = "$fieldName: '$str' must match the pattern '$pattern'"
            Result.Error(msg)
        }
    }
}