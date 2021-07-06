package com.mthaler.ordertaking.implementation

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.invalidNel
import arrow.core.nonEmptyListOf
import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo
import com.mthaler.ordertaking.common.ValidationError
import com.mthaler.ordertaking.domain.CustomerInfo
import com.mthaler.ordertaking.domain.EmailAddress
import com.mthaler.ordertaking.domain.PersonalName
import com.mthaler.ordertaking.domain.String50
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ImplementationTest: StringSpec({

    "toCustomerInfo" {
        toCustomerInfo(UnvalidatedCustomerInfo("a".repeat(60), "b", "a@b")) shouldBe ValidationError("FirstName must not be more than 50 chars").invalidNel()
        toCustomerInfo(UnvalidatedCustomerInfo("a", "b".repeat(60), "a@b")) shouldBe ValidationError("LastName must not be more than 50 chars").invalidNel()
        toCustomerInfo(UnvalidatedCustomerInfo("a".repeat(60), "b".repeat(60), "a@b")) shouldBe Invalid(
            nonEmptyListOf(ValidationError("FirstName must not be more than 50 chars"), ValidationError("LastName must not be more than 50 chars")))
        toCustomerInfo(UnvalidatedCustomerInfo("a", "b", "a@b")) shouldBe Valid(
            CustomerInfo(PersonalName(String50("a"), String50("b")), EmailAddress("a@b")))
    }
})