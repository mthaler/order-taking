package com.mthaler.ordertaking.validation

import com.mthaler.ordertaking.Result
import com.mthaler.ordertaking.domain.OrderId
import com.mthaler.ordertaking.domain.Price
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConstrainedTypeTest: StringSpec({

    "createInt" {
        createInt("test", ::OrderId, 0, 10, 5) shouldBe Result.Ok(OrderId(5))
        createInt("test", ::OrderId, 0, 10, -5) shouldBe Result.Error("test: Must not be less than 0")
        createInt("test", ::OrderId, 0, 10, 15) shouldBe Result.Error("test: Must not be greater than 10")
    }

    "createDecimal" {
        createDecimal("test", ::Price, 2.0.toBigDecimal(), 4.0.toBigDecimal(), 3.0.toBigDecimal()) shouldBe Result.Ok(Price(3.0.toBigDecimal()))
        createDecimal("test", ::Price, 2.0.toBigDecimal(), 4.0.toBigDecimal(), 1.0.toBigDecimal()) shouldBe Result.Error("test: Must not be less than 2.0")
        createDecimal("test", ::Price, 2.0.toBigDecimal(), 4.0.toBigDecimal(), 5.0.toBigDecimal()) shouldBe Result.Error("test: Must not be greater than 4.0")
    }
})