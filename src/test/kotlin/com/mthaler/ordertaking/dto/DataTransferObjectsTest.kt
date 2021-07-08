package com.mthaler.ordertaking.dto

import arrow.core.None
import arrow.core.Valid
import arrow.core.invalidNel
import com.mthaler.ordertaking.domain.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DataTransferObjectsTest: StringSpec({

    "toCustomerInfo" {
        CustomerInfoDto("John", "Doe", "john.doe@example.com").toCustomerInfo() shouldBe
                Valid(CustomerInfo(PersonalName(String50("John"), String50("Doe")), EmailAddress("john.doe@example.com")))
        CustomerInfoDto("John", "Doe", "john.doe").toCustomerInfo() shouldBe
                "EmailAddress: 'john.doe' must match the pattern '.+@.+'".invalidNel()
    }

    "toAddress" {
        AddressDto("Wall Street", "", "", "", "New York", "12345").toAddress() shouldBe
                Valid(Address(String50("Wall Street"), None, None, None, String50("New York"), ZipCode("12345")))
        AddressDto("Wall Street", "", "", "", "New York", "1234").toAddress() shouldBe
                "ZipCode: '1234' must match the pattern '\\d{5}'".invalidNel()
    }
})