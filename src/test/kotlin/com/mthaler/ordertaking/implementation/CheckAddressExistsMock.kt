package com.mthaler.ordertaking.implementation

import arrow.core.Valid
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import com.mthaler.ordertaking.common.UnvalidatedAddress

class CheckAddressExistsMock(val exists: Boolean): CheckAddressExists {

    override suspend fun checkAddressExists(unvalidatedAddress: UnvalidatedAddress): ValidatedNel<AddressValidationError, CheckedAddress> = when(exists) {
        true -> Valid(CheckedAddress(unvalidatedAddress))
        false -> AddressValidationError.AddressNotFound.invalidNel()
    }
}