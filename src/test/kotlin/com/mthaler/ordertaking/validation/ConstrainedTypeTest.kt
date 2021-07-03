package com.mthaler.ordertaking.validation

import com.mthaler.ordertaking.OrderId
import com.mthaler.ordertaking.Result
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConstrainedTypeTest: StringSpec({

    "createInt" {
        createInt("test", ::OrderId, 0, 10, 5) shouldBe Result.Ok(OrderId(5))
        createInt("test", ::OrderId, 0, 10, -5) shouldBe Result.Error("test: Must not be less than 0")
        createInt("test", ::OrderId, 0, 10, 15) shouldBe Result.Error("test: Must not be greater than 10")
    }
})