package com.mthaler.ordertaking.dto

import arrow.core.ValidatedNel
import arrow.core.zip
import com.mthaler.ordertaking.common.UnvalidatedCustomerInfo
import com.mthaler.ordertaking.domain.CustomerInfo
import com.mthaler.ordertaking.domain.EmailAddress
import com.mthaler.ordertaking.domain.PersonalName
import com.mthaler.ordertaking.domain.String50

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