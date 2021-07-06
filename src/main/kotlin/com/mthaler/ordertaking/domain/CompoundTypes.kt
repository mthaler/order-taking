package com.mthaler.ordertaking.domain

import arrow.core.Option

data class  PersonalName(val firstName: String50, val lastName: String50)

data class CustomerInfo(val name: PersonalName, val emailAddress: EmailAddress)

data class Address(val addressLine1 : String50, val addressLine2 : Option<String50>, val addressLine3 : Option<String50>, val addressLine4 : Option<String50>, val city: String50, val zipCode: ZipCode)