package com.mthaler.ordertaking.utils

import arrow.core.Validated

fun <E, A, B>Validated<E, A>.flatMap(f: (A) -> Validated<E, B>): Validated<E, B> = when(this) {
    is Validated.Invalid -> this
    is Validated.Valid -> f(this.value)
}
