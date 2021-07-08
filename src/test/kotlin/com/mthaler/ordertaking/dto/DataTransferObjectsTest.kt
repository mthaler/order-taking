package com.mthaler.ordertaking.dto

import arrow.core.Valid
import arrow.core.invalidNel
import com.mthaler.ordertaking.domain.CustomerInfo
import com.mthaler.ordertaking.domain.EmailAddress
import com.mthaler.ordertaking.domain.PersonalName
import com.mthaler.ordertaking.domain.String50
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DataTransferObjectsTest: StringSpec({

    "toCustomerInfo" {
        CustomerInfoDto("John", "Doe", "john.doe@example.com").toCustomerInfo() shouldBe
                Valid(CustomerInfo(PersonalName(String50("John"), String50("Doe")), EmailAddress("john.doe@example.com")))
        CustomerInfoDto("John", "Doe", "john.doe").toCustomerInfo() shouldBe
                "EmailAddress: 'john.doe' must match the pattern '.+@.+'".invalidNel()
    }
})