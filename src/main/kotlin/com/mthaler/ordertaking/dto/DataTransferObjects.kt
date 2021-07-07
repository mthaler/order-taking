package com.mthaler.ordertaking.dto

import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo

data class CustomerInfoDto(val firstName: String, val lastName: String, val emailAddress: String) {

    /// Convert the DTO into a UnvalidatedCustomerInfo object.
    /// This always succeeds because there is no validation.
    /// Used when importing an OrderForm from the outside world into the domain.
    fun toUnvalidatedCustomerInfo(): UnvalidatedCustomerInfo = UnvalidatedCustomerInfo(firstName, lastName, emailAddress)
}