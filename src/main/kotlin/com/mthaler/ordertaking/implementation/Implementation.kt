package com.mthaler.ordertaking.implementation

import arrow.core.*
import com.mthaler.ordertaking.common.*
import com.mthaler.ordertaking.domain.*
import com.mthaler.ordertaking.utils.combine
import com.mthaler.ordertaking.utils.flatMap

fun interface CheckProductCodeExists {

    operator fun invoke(productCode: ProductCode): Boolean
}

enum class AddressValidationError {
    InvalidFormat, AddressNotFound
}

data class CheckedAddress(val value: UnvalidatedAddress)

fun interface CheckAddressExists {

    suspend operator fun invoke(unvalidatedAddress: UnvalidatedAddress): ValidatedNel<AddressValidationError, CheckedAddress>
}

// ---------------------------
// Validated Order
// ---------------------------

data class ValidatedOrderLine(val orderLineId: OrderLineId, val productCode: ProductCode,val quantity: OrderQuantity)

data class ValidatedOrder(val orderId: OrderId, val customerInfo: CustomerInfo, val shippingAddress: Address, val billingAddress: Address, val lines: List<ValidatedOrderLine>)

fun interface ValidateOrder {

    suspend operator fun invoke(checkProductCodeExists: CheckProductCodeExists, checkAddressExists: CheckAddressExists, unvalidatedOrder: UnvalidatedOrder): ValidatedNel<ValidationError, ValidatedOrder>
}

// ---------------------------
// Pricing step
// ---------------------------

fun interface GetProductPrice {

    fun getProductPrice(productCode: ProductCode): Price
}

fun interface PriceOrder {

    fun priceOrder(getProductPrice : GetProductPrice, validatedOrder: ValidatedOrder): ValidatedNel<PricingError, PricedOrder>
}

// ---------------------------
// Send OrderAcknowledgment
// ---------------------------

data class HtmlString(val value: String)

data class OrderAcknowledgment(val emailAddress: EmailAddress, val letter: HtmlString)

fun interface CreateOrderAcknowledgmentLetter {

    fun createOrderAcknowledgmentLetter(pricedOrder: PricedOrder): HtmlString
}

/// Send the order acknowledgement to the customer
/// Note that this does NOT generate an Result-type error (at least not in this workflow)
/// because on failure we will continue anyway.
/// On success, we will generate a OrderAcknowledgmentSent event,
/// but on failure we won't.

enum class SendResult {
    Sent, NotSent
}

fun interface SendOrderAcknowledgment {

    fun sendOrderAcknowledgment(orderAcknowledgment: OrderAcknowledgment): SendResult
}

fun interface AcknowledgeOrder {

    fun acknowledgeOrder(createOrderAcknowledgmentLetter: CreateOrderAcknowledgmentLetter, sendOrderAcknowledgment: SendOrderAcknowledgment, pricedOrder: PricedOrder): Option<OrderAcknowledgmentSent>
}

// ---------------------------
// Create events
// ---------------------------

fun interface CreateEvents {

    fun createEvents(pricedOrder: PricedOrder, orderAcknowledgmentSent: Option<OrderAcknowledgmentSent>): List<PlaceOrderEvent>
}

// ======================================================
// Section 2 : Implementation
// ======================================================

// ---------------------------
// ValidateOrder step
// ---------------------------

fun UnvalidatedCustomerInfo.toCustomerInfo(): ValidatedNel<ValidationError, CustomerInfo> {
    val firstName = String50("FirstName", this.firstName).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val lastName = String50("LastName", this.lastName).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val emailAddress = EmailAddress("EmailAddress", this.emailAddress).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    return firstName.zip(lastName, emailAddress) { f, l, e ->
        CustomerInfo(PersonalName(f, l), e)
    }
}

fun CheckedAddress.toAddress(): ValidatedNel<ValidationError, Address> {
    val addressLine1 = String50("AddressLine1", this.value.addressLine1).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine2 = String50.createOption("AddressLine2", this.value.addressLine2).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine3 = String50.createOption("AddressLine3", this.value.addressLine3).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val addressLine4 = String50.createOption("AddressLine4", this.value.addressLine4).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val city = String50("City", this.value.city).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    val zipCode = ZipCode("ZipCode", this.value.zipCode).mapLeft { errors -> errors.map { str -> ValidationError(str) } }
    return addressLine1.zip(addressLine2, addressLine3, addressLine4, city, zipCode) { a1, a2, a3, a4, c, z ->
        Address(a1, a2, a3, a4, c, z)
    }
}

/// Call the checkAddressExists and convert the error to a ValidationError
suspend fun toCheckedAddress(checkAddress: CheckAddressExists, address: UnvalidatedAddress): ValidatedNel<ValidationError, CheckedAddress> =
    checkAddress(address).mapLeft { errors -> errors.map { err -> when(err) {
        AddressValidationError.AddressNotFound -> ValidationError("Address not found")
        AddressValidationError.InvalidFormat -> ValidationError("Address has bad format")
    } } }

fun String.toOrderId(): ValidatedNel<ValidationError, OrderId> = OrderId("OrderId", this).mapLeft { errors -> errors.map { str -> ValidationError(str) } }

/// Helper function for validateOrder
fun String.toOrderLineId(): ValidatedNel<ValidationError, OrderLineId> = OrderLineId("OrderLineId", this).mapLeft { errors -> errors.map { str -> ValidationError(str) } }

/// Helper function for validateOrder
fun toProductCode(checkProductCodeExists: CheckProductCodeExists, productCode: String): ValidatedNel<ValidationError, ProductCode> {

    fun checkProduct(productCode: ProductCode): ValidatedNel<ValidationError, ProductCode> = if (checkProductCodeExists(productCode)) Valid(productCode) else ValidationError("Invalid: $productCode").invalidNel()

    return ProductCode("ProductCode", productCode).mapLeft { errors -> errors.map { str -> ValidationError(str) } }.flatMap { checkProduct(it) }
}

/// Helper function for validateOrder
fun toOrderQuantity(productCode: ProductCode, quantity: Number): ValidatedNel<ValidationError, OrderQuantity> =
    OrderQuantity("OrderQuantity", productCode, quantity).mapLeft { errors -> errors.map { str -> ValidationError(str) } }

fun toValidatedOrderLine(checkProductCodeExists: CheckProductCodeExists, unvalidatedOrderLine: UnvalidatedOrderLine): ValidatedNel<ValidationError, ValidatedOrderLine> {
    val orderLineId = unvalidatedOrderLine.orderLineId.toOrderLineId()
    val productCode = toProductCode(checkProductCodeExists, unvalidatedOrderLine.productCode)
    val quantity = productCode.flatMap { toOrderQuantity(it, unvalidatedOrderLine.quantity) }
    return orderLineId.zip(productCode, quantity) { o, p, q ->
        ValidatedOrderLine(o, p, q)
    }
}

val validateOrder = object : ValidateOrder {

    override suspend operator fun invoke(
        checkProductCodeExists: CheckProductCodeExists,
        checkAddressExists: CheckAddressExists,
        unvalidatedOrder: UnvalidatedOrder
    ): ValidatedNel<ValidationError, ValidatedOrder> {
        val orderId = unvalidatedOrder.orderId.toOrderId()
        val customerInfo = unvalidatedOrder.customerInfo.toCustomerInfo()
        val checkedShippingAddress = toCheckedAddress(checkAddressExists, unvalidatedOrder.shippingAddress)
        val shippingAddress = checkedShippingAddress.flatMap { it.toAddress() }
        val checkedBillingAddress = toCheckedAddress(checkAddressExists, unvalidatedOrder.billingAddress)
        val billingAddress = checkedBillingAddress.flatMap { it.toAddress() }
        val lines = unvalidatedOrder.lines.map { toValidatedOrderLine(checkProductCodeExists, it) }.combine()
        return orderId.zip(customerInfo, shippingAddress, billingAddress, lines) { o, c, s, b, l ->
            ValidatedOrder(o, c, s, b, l)
        }
    }
}

// ---------------------------
// PriceOrder step
// ---------------------------

fun toPricedOrderLine(getProductPrice: GetProductPrice, validatedOrderLine: ValidatedOrderLine): ValidatedNel<PricingError, PricedOrderLine> {
    val qty = validatedOrderLine.quantity.value()
    val price = getProductPrice.getProductPrice(validatedOrderLine.productCode)
    val linePrice = (price * qty.toDouble()).mapLeft { errors -> errors.map { str -> PricingError(str) } }
    return linePrice.map { PricedOrderLine(validatedOrderLine.orderLineId, validatedOrderLine.productCode, validatedOrderLine.quantity, it) }
}

val priceOrder = object : PriceOrder {
    override fun priceOrder(
        getProductPrice: GetProductPrice,
        validatedOrder: ValidatedOrder
    ): ValidatedNel<PricingError, PricedOrder> {
        val lines = validatedOrder.lines.map { toPricedOrderLine(getProductPrice, it) }.combine()
        val amountToBill= lines.map { l -> l.map { it.linePrice } }.flatMap { it.sumPrices().mapLeft { errors -> errors.map { str -> PricingError(str) } } }
        return lines.zip(amountToBill) { l, a ->
            PricedOrder(validatedOrder.orderId, validatedOrder.customerInfo, validatedOrder.shippingAddress, validatedOrder.billingAddress, a, l)
        }
    }
}

// ---------------------------
// AcknowledgeOrder step
// ---------------------------

val acknowledgeOrder = object : AcknowledgeOrder {
    override fun acknowledgeOrder(
        createOrderAcknowledgmentLetter: CreateOrderAcknowledgmentLetter,
        sendOrderAcknowledgment: SendOrderAcknowledgment,
        pricedOrder: PricedOrder
    ): Option<OrderAcknowledgmentSent> {
        val letter = createOrderAcknowledgmentLetter.createOrderAcknowledgmentLetter(pricedOrder)
        val acknowledgement = OrderAcknowledgment(pricedOrder.customerInfo.emailAddress, letter)
        return when(sendOrderAcknowledgment.sendOrderAcknowledgment(acknowledgement)) {
            SendResult.Sent -> Some(OrderAcknowledgmentSent(pricedOrder.orderId, pricedOrder.customerInfo.emailAddress))
            SendResult.NotSent -> None
        }
    }
}

// ---------------------------
// Create events
// ---------------------------

fun createOrderPlacedEvent(placedOrder: PricedOrder): PlaceOrderEvent.OrderPlaced = PlaceOrderEvent.OrderPlaced(placedOrder)

fun createBillingEvent(placedOrder: PricedOrder): Option<PlaceOrderEvent.BillableOrderPlaced> {
    val billingAmount = placedOrder.amountToBill.value
    return if (billingAmount > 0) {
        Some(PlaceOrderEvent.BillableOrderPlaced(placedOrder.orderId, placedOrder.billingAddress, placedOrder.amountToBill))
    } else {
        None
    }
}

val createEvents = object : CreateEvents {

    override fun createEvents(
        pricedOrder: PricedOrder,
        orderAcknowledgmentSent: Option<OrderAcknowledgmentSent>
    ): List<PlaceOrderEvent> {
        val acknowledgmentEvents = orderAcknowledgmentSent.map { PlaceOrderEvent.AcknowledgmentSent(it) }.toList()
        val orderPlacedEvents = listOf(createOrderPlacedEvent(pricedOrder))
        val billingEvents = createBillingEvent(pricedOrder).toList()
        return acknowledgmentEvents + orderPlacedEvents + billingEvents
    }
}

// ---------------------------
// overall workflow
// ---------------------------

fun placeOrder(checkProductExists: CheckProductCodeExists,
               checkAddressExists: CheckAddressExists,
               getProductPrice: GetProductPrice,
               createOrderAcknowledgmentLetter: CreateOrderAcknowledgmentLetter,
               sendOrderAcknowledgment: SendOrderAcknowledgment): PlaceOrder = object : PlaceOrder {
    override suspend fun placeOrder(unvalidatedOrder: UnvalidatedOrder): ValidatedNel<PlaceOrderError, List<PlaceOrderEvent>> {
        val validateOrder = validateOrder(checkProductExists, checkAddressExists, unvalidatedOrder).mapLeft { errors -> errors.map { e -> PlaceOrderError.Validation(e) } }
        val pricedOrder = validateOrder.flatMap { priceOrder.priceOrder(getProductPrice, it).mapLeft { errors -> errors.map { e -> PlaceOrderError.Pricing(e) } } }
        val acknowledgementOption = pricedOrder.map { acknowledgeOrder.acknowledgeOrder(createOrderAcknowledgmentLetter, sendOrderAcknowledgment, it) }
        return pricedOrder.zip(acknowledgementOption) { p, a ->
            createEvents.createEvents(p, a)
        }
    }
}