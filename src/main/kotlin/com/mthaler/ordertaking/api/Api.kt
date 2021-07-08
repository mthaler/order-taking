package com.mthaler.ordertaking.api

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.ValidatedNel
import com.mthaler.ordertaking.common.PlaceOrderError
import com.mthaler.ordertaking.common.PlaceOrderEvent
import com.mthaler.ordertaking.domain.Price
import com.mthaler.ordertaking.dto.PlaceOrderErrorDto
import com.mthaler.ordertaking.implementation.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mthaler.ordertaking.dto.OrderFormDto
import com.mthaler.ordertaking.dto.PlaceOrderEventDto

// ======================================================
// This file contains the JSON API interface to the PlaceOrder workflow
//
// 1) The HttpRequest is turned into a DTO, which is then turned into a Domain object
// 2) The main workflow function is called
// 3) The output is turned into a DTO which is turned into a HttpResponse
// ======================================================

data class JsonString(val value: String)

/// Very simplified version!
data class HttpRequest(val action: String, val uri: String, val body: JsonString)

/// Very simplified version!
data class HttpResponse(val httpStatusCode: Int, val body: JsonString)

/// An API takes a HttpRequest as input and returns a async response
fun interface PlaceOrderApi {

    suspend operator fun invoke(request: HttpRequest): HttpResponse
}

// =============================
// Implementation
// =============================

// setup dummy dependencies

// dummy implementation
val checkProductExists: CheckProductCodeExists = CheckProductCodeExists { unvalidatedAddress -> true }

// dummy implementation
val checkAddressExists: CheckAddressExists = CheckAddressExists { unvalidatedAddress -> Valid(CheckedAddress(unvalidatedAddress)) }

// dummy implementation
val getProductPrice: GetProductPrice = GetProductPrice { productCode -> Price.unsafeCreate(1000.0) }

val createOrderAcknowledgmentLetter: CreateOrderAcknowledgmentLetter = CreateOrderAcknowledgmentLetter { pricedOrder -> HtmlString("some text") }

val sendOrderAcknowledgment: SendOrderAcknowledgment = SendOrderAcknowledgment { orderAcknowledgment -> SendResult.Sent }

// -------------------------------
// workflow
// -------------------------------

/// This function converts the workflow output into a HttpResponse

fun workflowResultToHttpResponse(result: ValidatedNel<PlaceOrderError, List<PlaceOrderEvent>>): HttpResponse {
    when(result) {
        is Valid -> {
            // turn domain events into dtos
            val dtos = result.value.map { PlaceOrderEventDto.fromDomain(it) }.toTypedArray()
            // and serialize to JSON
            val json = jacksonObjectMapper().writeValueAsString(dtos)
            return HttpResponse(200, JsonString(json))
        }
        is Invalid -> {
            // turn domain errors into a dto
            val dtos = result.value.map { PlaceOrderErrorDto.fromDomain(it) }.toTypedArray()
            // and serialize to JSON
            val json = jacksonObjectMapper().writeValueAsString(dtos)
            return HttpResponse(401, JsonString(json))
        }
    }
}

val placeOrderApi: PlaceOrderApi = PlaceOrderApi { request ->

    // following the approach in "A Complete Serialization Pipeline" in chapter 11

    // start with a string
    val orderFormJson = request.body.value
    val orderForm = jacksonObjectMapper().readValue(orderFormJson, OrderFormDto::class.java)
    // convert to domain object
    val unvalidatedOrder = orderForm.toUnvalidatedOrder()

    // setup the dependencies. See "Injecting Dependencies" in chapter 9
    val workflow = placeOrder(checkProductExists, checkAddressExists, getProductPrice, createOrderAcknowledgmentLetter, sendOrderAcknowledgment)

    // now we are in the pure domain
    val result = workflow.placeOrder(unvalidatedOrder)

    workflowResultToHttpResponse(result)
}