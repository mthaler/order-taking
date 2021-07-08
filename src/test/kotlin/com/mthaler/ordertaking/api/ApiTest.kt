package com.mthaler.ordertaking.api

import arrow.core.None
import arrow.core.Valid
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mthaler.ordertaking.common.PlaceOrderEvent
import com.mthaler.ordertaking.common.PricedOrder
import com.mthaler.ordertaking.common.PricedOrderLine
import com.mthaler.ordertaking.domain.*
import com.mthaler.ordertaking.dto.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ApiTest: StringSpec({

    "workflowResultToHttpResponse" {
        val event = PlaceOrderEvent.OrderPlaced(
            PricedOrder(
                OrderId("test"), CustomerInfo(PersonalName(String50("John"), String50("Doe")), EmailAddress("john.doe@example.com")),
            Address(String50("Wall Street"), None, None, None, String50("New York"), ZipCode("12345")), Address(String50("Wall Street"), None, None, None, String50("New York"), ZipCode("12345")),
            BillingAmount(1000.0), listOf(PricedOrderLine(OrderLineId("test"), ProductCode.WidgetCode("W1234"), OrderQuantity.UnitQuantity(25), Price(40.0))))
        )
        val response = workflowResultToHttpResponse(Valid(listOf(event)))
        response.httpStatusCode shouldBe 200
        response.body.value shouldBe
                """[{"value":{"OrderPlaced":{"orderId":"test","customerInfo":{"firstName":"John","lastName":"Doe","emailAddress":"john.doe@example.com"},"shippingAddress":{"addressLine1":"Wall Street","addressLine2":"","addressLine3":"","addressLine4":"","city":"New York","zipCode":"12345"},"billingAddress":{"addressLine1":"Wall Street","addressLine2":"","addressLine3":"","addressLine4":"","city":"New York","zipCode":"12345"},"amountToBill":1000.0,"lines":[{"orderLineId":"test","quantity":25,"linePrice":40.0,"productCode":"W1234"}]}}}]"""
    }

    "OrderFormDto.toJson" {
        val dto = OrderFormDto("test", CustomerInfoDto("John", "Doe", "john.doe@example.com"), AddressDto("Wall Street", "", "", "", "New York", "12345"),
            AddressDto("Wall Street", "", "", "", "New York", "12345"), listOf(OrderFormLineDto("test", "W1234", 25)))
        jacksonObjectMapper().writeValueAsString(dto) shouldBe
                """{"orderId":"test","customerInfo":{"firstName":"John","lastName":"Doe","emailAddress":"john.doe@example.com"},"shippingAddress":{"addressLine1":"Wall Street","addressLine2":"","addressLine3":"","addressLine4":"","city":"New York","zipCode":"12345"},"billingAddress":{"addressLine1":"Wall Street","addressLine2":"","addressLine3":"","addressLine4":"","city":"New York","zipCode":"12345"},"lines":[{"orderLineId":"test","productCode":"W1234","quantity":25}]}"""
    }
})