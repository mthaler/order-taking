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

fun workflowResultToHttpReponse(result: ValidatedNel<PlaceOrderError, List<PlaceOrderEvent>>): HttpResponse {
    when(result) {
        is Valid -> {
            TODO()
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