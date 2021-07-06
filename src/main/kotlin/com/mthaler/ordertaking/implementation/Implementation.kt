package com.mthaler.ordertaking.implementation

import com.mthaler.ordertaking.domain.*

fun interface CheckProductCodeExists {

    fun checkExists(productCode: ProductCode): Boolean
}

enum class AddressValidationError {
    InvalidFormat, AddressNotFound
}

// ---------------------------
// Validated Order
// ---------------------------

data class ValidatedOrderLine(val orderLineId: OrderLineId, val productCode: ProductCode,val quantity: OrderQuantity)

data class ValidatedOrder(val orderId: OrderId, val customerInfo: CustomerInfo, val shippingAddress: Address, val billingAddress: Address, val lines: List<ValidatedOrderLine>)

// ---------------------------
// Pricing step
// ---------------------------

fun interface GetProductPrice {

    fun getProductPrice(productCode: ProductCode): Price
}

// ---------------------------
// Send OrderAcknowledgment
// ---------------------------

data class HtmlString(val value: String)

data class OrderAcknowledgment(val emailAddress: EmailAddress, val letter: HtmlString)

/// Send the order acknowledgement to the customer
/// Note that this does NOT generate an Result-type error (at least not in this workflow)
/// because on failure we will continue anyway.
/// On success, we will generate a OrderAcknowledgmentSent event,
/// but on failure we won't.

enum class SendResult {
    Sent, NotSent
}

// ---------------------------
// Create events
// ---------------------------

// ======================================================
// Section 2 : Implementation
// ======================================================

// ---------------------------
// ValidateOrder step
// ---------------------------