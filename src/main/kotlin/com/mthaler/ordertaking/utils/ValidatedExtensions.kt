package com.mthaler.ordertaking.utils

import arrow.core.*

fun <E, A, B>Validated<E, A>.flatMap(f: (A) -> Validated<E, B>): Validated<E, B> = when(this) {
    is Validated.Invalid -> this
    is Validated.Valid -> f(this.value)
}

fun <E, A>List<ValidatedNel<E, A>>.combine(): ValidatedNel<E, List<A>> {
    val errors: MutableList<E> = ArrayList()
    val lines: MutableList<A> = ArrayList()
    for (v in this) {
        when(v) {
            is Invalid -> errors.addAll(v.value)
            is Valid -> lines.add(v.value)
        }
    }
    return if (errors.isNotEmpty()) {
        Invalid(NonEmptyList.fromListUnsafe(errors))
    } else {
        Valid(lines)
    }
}
