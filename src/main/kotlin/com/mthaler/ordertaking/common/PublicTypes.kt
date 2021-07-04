package com.mthaler.ordertaking.common

import com.mthaler.ordertaking.domain.*
import com.mthaler.ordertaking.Result
import java.net.URI

// ==================================
// This file contains the definitions of PUBLIC types (exposed at the boundary of the bounded context)
// related to the PlaceOrder workflow
// ==================================

// ------------------------------------
// inputs to the workflow

data class UnvalidatedCustomerInfo(val firstName: String, val lastName: String, val emailAddress: String)

data class UnvalidatedAddress(val addressLine1: String, val addressLine2: String, val addressLine3: String, val addressLine4: String, val city: String, val zipCode : String)

data class UnvalidatedOrderLine(val orderLineId: String, val productCode: String, val quantity: Double)

data class UnvalidatedOrder(val orderId: String, val customerInfo: UnvalidatedCustomerInfo, val shippingAddress: UnvalidatedAddress, val billingAddress: UnvalidatedAddress, val lines : List<UnvalidatedOrderLine>)

// ------------------------------------
// outputs from the workflow (success case)

/// Event will be created if the Acknowledgment was successfully posted

data class OrderAcknowledgmentSent(val orderId: OrderId, val emailAddress: EmailAddress)

// priced state
data class PricedOrderLine(val orderLineId: OrderLineId, val productCode: ProductCode, val quantity: OrderQuantity, val linePrice: Price)

data class PricedOrder(val orderId: OrderId, val customerInfo: CustomerInfo, val shippingAddress: Address, val billingAddress: Address, val amountToBill: BillingAmount, val lines : List<PricedOrderLine>)

/// Event to send to shipping context
typealias OrderPlaced = PricedOrder

/// The possible events resulting from the PlaceOrder workflow
/// Not all events will occur, depending on the logic of the workflow
sealed class PlaceOrderEvent {

    data class PricedOrder(val value: OrderPlaced): PlaceOrderEvent()

    /// Event to send to billing context
    /// Will only be created if the AmountToBill is not zero
    data class BillableOrderPlaced(val orderId: OrderId, val billingAddress: Address, val amountToBill: BillingAmount): PlaceOrderEvent()

    data class AcknowledgmentSent(val value: OrderAcknowledgmentSent): PlaceOrderEvent()
}

// ------------------------------------
// error outputs

/// All the things that can go wrong in this workflow
data class ValidationError(val value: String)

data class PricingError(val value: String)

data class ServiceInfo(val name: String, val endoint: URI)

data class RemoteServiceError(val service: ServiceInfo, val exception: Exception)

sealed class PlaceOrderError {
    data class Validation(val value: ValidationError): PlaceOrderError()

    data class Pricing(val value: PricingError): PlaceOrderError()

    data class RemoteService(val value: RemoteServiceError): PlaceOrderError()
}

// ------------------------------------
// the workflow itself

fun interface PlaceOrder {

    fun placeOrder(order: UnvalidatedOrder): Result<List<PlaceOrderEvent>, PlaceOrderError>
}
