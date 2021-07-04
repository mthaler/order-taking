package com.mthaler.ordertaking

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResultTest: StringSpec({

    "map" {
        Result.Ok(42).map { it.toString() } shouldBe Result.Ok("42")
    }

    "mapError" {
        Result.Error("test").mapError { Pair(it, it) } shouldBe Result.Error(Pair("test", "test"))
    }

    "foreach" {
        var i = -1
        Result.Ok(42).foreach { i = it }
        i shouldBe 42
        i = -1
        Result.Error("test").foreach { i = it }
        i shouldBe -1
    }
})