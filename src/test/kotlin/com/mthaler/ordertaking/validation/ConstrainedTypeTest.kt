package com.mthaler.ordertaking.validation

import arrow.core.Validated
import arrow.core.invalidNel
import com.mthaler.ordertaking.domain.OrderQuantity.UnitQuantity
import com.mthaler.ordertaking.domain.Price
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConstrainedTypeTest: StringSpec({

    "createInt" {
        createInt("test", ::UnitQuantity, 0, 10, 5) shouldBe Validated.Valid(UnitQuantity(5))
        createInt("test", ::UnitQuantity, 0, 10, -5) shouldBe "test: Must not be less than 0".invalidNel()
        createInt("test", ::UnitQuantity, 0, 10, 15) shouldBe "test: Must not be greater than 10".invalidNel()
    }

    "createDecimal" {
        createDecimal("test", ::Price, 2.0, 4.0, 3.0) shouldBe Validated.Valid(Price(3.0))
        createDecimal("test", ::Price, 2.0, 4.0, 1.0) shouldBe "test: Must not be less than 2.0".invalidNel()
        createDecimal("test", ::Price, 2.0, 4.0, 5.0) shouldBe "test: Must not be greater than 4.0".invalidNel()
    }
})