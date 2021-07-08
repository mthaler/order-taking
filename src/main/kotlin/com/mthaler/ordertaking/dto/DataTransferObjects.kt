package com.mthaler.ordertaking.dto

import arrow.core.ValidatedNel
import arrow.core.getOrElse
import arrow.core.valid
import arrow.core.zip
import com.mthaler.ordertaking.common.*
import com.mthaler.ordertaking.domain.*
import com.mthaler.ordertaking.validation.createInt

data class CustomerInfoDto(val firstName: String, val lastName: String, val emailAddress: String) {

    /// Convert the DTO into a UnvalidatedCustomerInfo object.
    /// This always succeeds because there is no validation.
    /// Used when importing an OrderForm from the outside world into the domain.
    fun toUnvalidatedCustomerInfo(): UnvalidatedCustomerInfo = UnvalidatedCustomerInfo(firstName, lastName, emailAddress)

    /// Convert the DTO into a CustomerInfo object
    /// Used when importing from the outside world into the domain, eg loading from a database
    fun toCustomerInfo(): ValidatedNel<String, CustomerInfo> {
        val firstName = String50("FirstName", firstName)
        val lastName = String50("LastName", lastName)
        val email = EmailAddress("EmailAddress", emailAddress)
        return firstName.zip(lastName, email) { f, l, e ->
            val name = PersonalName(f, l)
            CustomerInfo(name, e)
        }
    }

    companion object {

        /// Convert a CustomerInfo object into the corresponding DTO.
        /// Used when exporting from the domain to the outside world.
        fun fromCustomerInfo(domainObj: CustomerInfo): CustomerInfoDto = CustomerInfoDto(domainObj.name.firstName.value, domainObj.name.lastName.value, domainObj.emailAddress.value)
    }
}

//===============================================
// DTO for Address
//===============================================

data class AddressDto(val addressLine1: String, val addressLine2: String, val addressLine3: String, val addressLine4: String, val city: String, val zipCode : String) {

    /// Convert the DTO into a UnvalidatedAddress
    /// This always succeeds because there is no validation.
    /// Used when importing an OrderForm from the outside world into the domain.
    fun toUnvalidatedAddress(): UnvalidatedAddress = UnvalidatedAddress(addressLine1, addressLine2, addressLine3, addressLine4, city, zipCode)

    /// Convert the DTO into a Address object
    /// Used when importing from the outside world into the domain, eg loading from a database.
    fun toAddress(): ValidatedNel<String, Address> {
        val addressLine1 = String50("AddressLine1", addressLine1)
        val addressLine2 = String50.createOption("AddressLine2", addressLine2)
        val addressLine3 = String50.createOption("AddressLine3", addressLine3)
        val addressLine4 = String50.createOption("AddressLine4", addressLine4)
        val city = String50("City", city)
        val zipCode = ZipCode("ZipCode", zipCode)
        return addressLine1.zip(addressLine2, addressLine3, addressLine4, city, zipCode) { a1, a2, a3, a4, c, z ->
            Address(a1, a2, a3, a4, c, z)
        }
    }

    companion object {

        /// Convert a Address object into the corresponding DTO.
        /// Used when exporting from the domain to the outside world.
        fun fromAddress(domainObj: Address): AddressDto = AddressDto(domainObj.addressLine1.value,
            domainObj.addressLine2.map { it.value }.getOrElse { "" },
            domainObj.addressLine3.map { it.value }.getOrElse { "" },
            domainObj.addressLine4.map { it.value }.getOrElse { "" },
            domainObj.city.value,
            domainObj.zipCode.value
        )
    }
}

//===============================================
// DTOs for OrderLines
//===============================================

data class OrderFormLineDto(val orderLineId: String, val productCode: String, val quantity : Number) {

    /// Convert the OrderFormLine into a UnvalidatedOrderLine
    /// This always succeeds because there is no validation.
    /// Used when importing an OrderForm from the outside world into the domain.
    fun toUnvalidatedOrderLine(): UnvalidatedOrderLine = UnvalidatedOrderLine(this.orderLineId, this.productCode, this.quantity)
}

//===============================================
// DTOs for PricedOrderLines
//===============================================

data class PricedOrderLineDto(val orderLineId: String, val ProductCode: String, val quantity: Number, val linePrice: Double) {


    companion object {
        /// Convert a PricedOrderLine object into the corresponding DTO.
        /// Used when exporting from the domain to the outside world.
        fun fromDomain(domainObj: PricedOrderLine): PricedOrderLineDto = PricedOrderLineDto(domainObj.orderLineId.value, domainObj.productCode.value(), domainObj.quantity.value(), domainObj.linePrice.value)
    }
}

//===============================================
// DTO for OrderForm
//===============================================

data class OrderFormDto(val orderId: String, val customerInfo: CustomerInfoDto, val shippingAddress: AddressDto, val billingAddress: AddressDto, val lines : List<OrderFormLineDto>) {

    /// Convert the OrderForm into a UnvalidatedOrder
    /// This always succeeds because there is no validation.
    fun toUnvalidatedOrder(): UnvalidatedOrder = UnvalidatedOrder(orderId, customerInfo.toUnvalidatedCustomerInfo(), shippingAddress.toUnvalidatedAddress(), billingAddress.toUnvalidatedAddress(), lines.map { it.toUnvalidatedOrderLine() })
}

//===============================================
// DTO for OrderPlaced event
//===============================================

/// Event to send to shipping context
data class OrderPlacedDto(val orderId: String, val customerInfo: CustomerInfoDto, val shippingAddress: AddressDto, val billingAddress: AddressDto, val amountToBill: Double, val lines : List<PricedOrderLineDto>) {

    companion object {

        /// Convert a OrderPlaced object into the corresponding DTO.
        /// Used when exporting from the domain to the outside world.
        fun fromDomain(domainObj: PlaceOrderEvent.OrderPlaced): OrderPlacedDto =
            OrderPlacedDto(
                domainObj.value.orderId.value,
                CustomerInfoDto.fromCustomerInfo(domainObj.value.customerInfo),
                AddressDto.fromAddress(domainObj.value.shippingAddress),
                AddressDto.fromAddress(domainObj.value.billingAddress),
                domainObj.value.amountToBill.value,
                domainObj.value.lines.map { PricedOrderLineDto.fromDomain(it) }
            )
    }
}