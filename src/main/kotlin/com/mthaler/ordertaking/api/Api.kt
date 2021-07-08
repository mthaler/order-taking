package com.mthaler.ordertaking.api

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