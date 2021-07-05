package com.mthaler.ordertaking.validation

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Validated

/// Create a constrained string using the constructor provided
/// Return Error if input is null, empty, or length > maxLen
fun <T>createString(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): Validated<String, T> {
    return when {
        str.isEmpty() -> {
            val msg = "$fieldName must not be empty"
            Validated.Invalid(msg)
        }
        str.length > maxLen -> {
            val msg =  "$fieldName must not be more than $maxLen chars"
            Validated.Invalid(msg)
        }
        else -> {
            Validated.Valid(ctor(str))
        }
    }
}

/// Create a optional constrained string using the constructor provided
/// Return None if input is null, empty.
/// Return error if length > maxLen
/// Return Some if the input is valid
fun <T>createStringOption(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): Validated<String, Option<T>> {
    return when {
        str.isEmpty() -> {
            Validated.Valid(None)
        }
        str.length > maxLen -> {
            val msg =  "$fieldName must not be more than $maxLen chars"
            Validated.Invalid(msg)
        }
        else -> {
            Validated.Valid(Some(ctor(str)))
        }
    }
}

/// Create a constrained integer using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createInt(fieldName: String, ctor: (Int) -> T, minVal: Int,  maxVal: Int, i: Int): Validated<String, T> {
    return when {
        i < minVal -> {
            val msg = "$fieldName: Must not be less than $minVal"
            Validated.Invalid(msg)
        }
        i > maxVal -> {
            val msg = "$fieldName: Must not be greater than $maxVal"
            Validated.Invalid(msg)
        }
        else -> {
            Validated.Valid(ctor(i))
        }
    }
}

/// Create a constrained decimal using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createDecimal(fieldName: String, ctor: (Double) -> T, minVal: Double,  maxVal: Double, i: Double): Validated<String, T> {
    return when {
        i < minVal -> {
            val msg = "$fieldName: Must not be less than $minVal"
            Validated.Invalid(msg)
        }
        i > maxVal -> {
            val msg = "$fieldName: Must not be greater than $maxVal"
            Validated.Invalid(msg)
        }
        else -> {
            Validated.Valid(ctor(i))
        }
    }
}

/// Create a constrained string using the constructor provided
/// Return Error if input is null. empty, or does not match the regex pattern
fun <T>createLike(fieldName: String, ctor: (String) -> T, pattern: String, str: String): Validated<String, T> {
    return when {
        str.isEmpty() -> {
            val msg = "$fieldName must not be null or empty"
            Validated.Invalid(msg)
        }
        pattern.toRegex().matches(str) -> {
            Validated.Valid(ctor(str))
        }
        else -> {
            val msg = "$fieldName: '$str' must match the pattern '$pattern'"
            Validated.Invalid(msg)
        }
    }
}