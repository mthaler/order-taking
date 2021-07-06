package com.mthaler.ordertaking.implementation

import arrow.core.*
import com.mthaler.ordertaking.common.UnvalidatedAddress
import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo
import com.mthaler.ordertaking.common.ValidationError
import com.mthaler.ordertaking.domain.*
import com.mthaler.ordertaking.utils.flatMap

fun interface CheckProductCodeExists {

    fun checkExists(productCode: ProductCode): Boolean
}

enum class AddressValidationError {
    InvalidFormat, AddressNotFound
}

data class CheckedAddress(val value: UnvalidatedAddress)

fun interface CheckAddressExists {

    suspend fun checkAddressExists(unvalidatedAddress: UnvalidatedAddress): ValidatedNel<AddressValidationError, CheckedAddress>
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

fun toCustomerInfo(unvalidatedCustomerInfo: UnvalidatedCustomerInfo): ValidatedNel<ValidationError, CustomerInfo> {
    val firstName = String50("FirstName", unvalidatedCustomerInfo.firstName).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val lastName = String50("LastName", unvalidatedCustomerInfo.lastName).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val emailAddress = EmailAddress("EmailAddress", unvalidatedCustomerInfo.emailAddress).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    return firstName.zip(lastName, emailAddress) { f, l, e ->
        CustomerInfo(PersonalName(f, l), e)
    }
}

fun toAddress(unvalidatedAddress: CheckedAddress): ValidatedNel<ValidationError, Address> {
    val addressLine1 = String50("AddressLine1", unvalidatedAddress.value.addressLine1).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine2 = String50.createOption("AddressLine2", unvalidatedAddress.value.addressLine2).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine3 = String50.createOption("AddressLine3", unvalidatedAddress.value.addressLine3).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine4 = String50.createOption("AddressLine4", unvalidatedAddress.value.addressLine4).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val city = String50("City", unvalidatedAddress.value.city).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val zipCode = ZipCode("ZipCode", unvalidatedAddress.value.zipCode).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    return addressLine1.zip(addressLine2, addressLine3, addressLine4, city, zipCode) { a1, a2, a3, a4, c, z ->
        Address(a1, a2, a3, a4, c, z)
    }
}

/// Call the checkAddressExists and convert the error to a ValidationError
suspend fun toCheckedAddress(checkAddress: CheckAddressExists, address: UnvalidatedAddress): ValidatedNel<ValidationError, CheckedAddress> =
    checkAddress.checkAddressExists(address).mapLeft { errors -> errors.map { err -> when(err) {
        AddressValidationError.AddressNotFound -> ValidationError("Address not found")
        AddressValidationError.InvalidFormat -> ValidationError("Address has bad format")
    } } }

fun toOrderId(orderId: String): ValidatedNel<ValidationError, OrderId> = OrderId("OrderId", orderId).mapLeft { errors -> errors.map { str -> ValidationError(str) } }

/// Helper function for validateOrder
fun toOrderLineId(orderLineId: String): ValidatedNel<ValidationError, OrderLineId> = OrderLineId("OrderLineId", orderLineId).mapLeft { errors -> errors.map { str -> ValidationError(str) } }

/// Helper function for validateOrder
fun toProductCode(checkProductCodeExists: CheckProductCodeExists, productCode: String): ValidatedNel<ValidationError, ProductCode> {

    fun checkProduct(productCode: ProductCode): ValidatedNel<ValidationError, ProductCode> = if (checkProductCodeExists.checkExists(productCode)) Valid(productCode) else ValidationError("Invalid: $productCode").invalidNel()

    return ProductCode("ProductCode", productCode).mapLeft { errors -> errors.map { str -> ValidationError(str) } }.flatMap { checkProduct(it) }
}

/// Helper function for validateOrder
fun toOrderQuantity(productCode: ProductCode, quantity: Number): ValidatedNel<ValidationError, OrderQuantity> =
    OrderQuantity("OrderQuantity", productCode, quantity).mapLeft { errors -> errors.map { str -> ValidationError(str) } }