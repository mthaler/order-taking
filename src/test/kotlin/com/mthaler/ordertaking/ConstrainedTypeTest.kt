package com.mthaler.ordertaking

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConstrainedTypeTest: StringSpec({

    "createInt" {
        ConstrainedType.createInt("test", ::OrderId, 0, 10, 5) shouldBe Result.Ok(OrderId(5))
        ConstrainedType.createInt("test", ::OrderId, 0, 10, -5) shouldBe Result.Error("test: Must not be less than 0")
        ConstrainedType.createInt("test", ::OrderId, 0, 10, 15) shouldBe Result.Error("test: Must not be greater than 10")
    }
})