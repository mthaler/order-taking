package com.mthaler.ordertaking.validation

import arrow.core.*

/// Create a constrained string using the constructor provided
/// Return Error if input is null, empty, or length > maxLen
fun <T>createString(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): ValidatedNel<String, T> = when {
        str.isEmpty() -> "$fieldName must not be empty".invalidNel()
        str.length > maxLen -> "$fieldName must not be more than $maxLen chars".invalidNel()
        else -> Validated.Valid(ctor(str))
    }

/// Create a optional constrained string using the constructor provided
/// Return None if input is null, empty.
/// Return error if length > maxLen
/// Return Some if the input is valid
fun <T>createStringOption(fieldName: String, ctor: (String) -> T, maxLen: Int, str: String): ValidatedNel<String, Option<T>> = when {
        str.isEmpty() -> Validated.Valid(None)
        str.length > maxLen -> "$fieldName must not be more than $maxLen chars".invalidNel()
        else -> Validated.Valid(Some(ctor(str)))
    }

/// Create a constrained integer using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createInt(fieldName: String, ctor: (Int) -> T, minVal: Int,  maxVal: Int, i: Int): ValidatedNel<String, T> = when {
        i < minVal -> "$fieldName: Must not be less than $minVal".invalidNel()
        i > maxVal -> "$fieldName: Must not be greater than $maxVal".invalidNel()
        else -> Validated.Valid(ctor(i))
    }

/// Create a constrained decimal using the constructor provided
/// Return Error if input is less than minVal or more than maxVal
fun <T>createDecimal(fieldName: String, ctor: (Double) -> T, minVal: Double,  maxVal: Double, i: Double): ValidatedNel<String, T> = when {
        i < minVal -> "$fieldName: Must not be less than $minVal".invalidNel()
        i > maxVal -> "$fieldName: Must not be greater than $maxVal".invalidNel()
        else -> Validated.Valid(ctor(i))
    }

/// Create a constrained string using the constructor provided
/// Return Error if input is null. empty, or does not match the regex pattern
fun <T>createLike(fieldName: String, ctor: (String) -> T, pattern: String, str: String): ValidatedNel<String, T> = when {
        str.isEmpty() -> "$fieldName must not be null or empty".invalidNel()
        pattern.toRegex().matches(str) -> Validated.Valid(ctor(str))
        else -> "$fieldName: '$str' must match the pattern '$pattern'".invalidNel()
    }