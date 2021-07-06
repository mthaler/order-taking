package com.mthaler.ordertaking.implementation

import arrow.core.*
import com.mthaler.ordertaking.common.UnvalidatedAddress
import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo
import com.mthaler.ordertaking.common.ValidationError
import com.mthaler.ordertaking.domain.*
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

    "toAddress" {
//        toAddress(CheckedAddress(UnvalidatedAddress("Wall Street", "", "", "", "Mew York", "12345"))) shouldBe
//                Valid(Address(String50("Wall Street"), None, None, None, String50("New York"), ZipCode("12345")))
    }
})