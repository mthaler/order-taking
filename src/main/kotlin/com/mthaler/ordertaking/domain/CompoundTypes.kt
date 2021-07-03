package com.mthaler.ordertaking.domain

data class  PersonalName(val firstName: String50, val lastName: String50)

data class CustomerInfo(val name: PersonalName, val emailAddress: EmailAddress)

data class Address(val addressLine1 : String50, val addressLine2 : String50?, val addressLine3 : String50?, val addressLine4 : String50?, val city: String50, val zipCode: ZipCode)