package com.mthaler.ordertaking.implementation

import arrow.core.*
import com.mthaler.ordertaking.common.UnvalidatedAddress
import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo
import com.mthaler.ordertaking.common.ValidationError
import com.mthaler.ordertaking.domain.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        toAddress(CheckedAddress(UnvalidatedAddress("Wall Street", "", "", "", "New York", "12345"))) shouldBe
                Valid(Address(String50("Wall Street"), None, None, None, String50("New York"), ZipCode("12345")))
    }

    "toCheckedAddress" {
        runBlocking {
            launch {
                val address = UnvalidatedAddress("Wall Street", "", "", "", "New York", "12345")
                toCheckedAddress(CheckAddressExistsMock(false), address) shouldBe ValidationError("Address not found").invalidNel()
                toCheckedAddress(CheckAddressExistsMock(true), address) shouldBe Valid(CheckedAddress(address))

            }
        }
    }

    "toOrderId" {
        toOrderId("test") shouldBe Valid(OrderId("test"))
        toOrderId("a".repeat(60)) shouldBe ValidationError("OrderId must not be more than 50 chars").invalidNel()
    }

    "toOrderLineId" {
        toOrderLineId("test") shouldBe Valid(OrderLineId("test"))
        toOrderLineId("a".repeat(60)) shouldBe ValidationError("OrderLineId must not be more than 50 chars").invalidNel()
    }

    "toProductCode" {
        toProductCode({ productCode -> true }, "W1234") shouldBe Valid(ProductCode.WidgetCode("W1234"))
        toProductCode({ productCode -> true }, "G123") shouldBe Valid(ProductCode.GizmoCode("G123"))
        toProductCode({ productCode -> true }, "foo") shouldBe ValidationError("ProductCode: Format not recognized 'foo'").invalidNel()
        toProductCode({ productCode -> false }, "G123") shouldBe ValidationError("Invalid: GizmoCode(value=G123)").invalidNel()
        toProductCode({ productCode -> false }, "foo") shouldBe ValidationError("ProductCode: Format not recognized 'foo'").invalidNel()
    }

    "toOrderQuantity" {
        toOrderQuantity(ProductCode.WidgetCode("W1234"), 25) shouldBe Valid(OrderQuantity.UnitQuantity(25))
        toOrderQuantity(ProductCode.WidgetCode("W1234"), 0) shouldBe ValidationError("OrderQuantity: Must not be less than 1").invalidNel()
        toOrderQuantity(ProductCode.GizmoCode("G123"), 25.0) shouldBe Valid(OrderQuantity.KilogramQuantity(25.0))
        toOrderQuantity(ProductCode.GizmoCode("G123"), 0.0) shouldBe ValidationError("OrderQuantity: Must not be less than 0.05").invalidNel()
    }
})