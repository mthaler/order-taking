package com.mthaler.ordertaking.domain

import com.mthaler.ordertaking.Option
import com.mthaler.ordertaking.Result
import com.mthaler.ordertaking.validation.createLike
import com.mthaler.ordertaking.validation.createString
import com.mthaler.ordertaking.validation.createStringOption
import java.math.BigDecimal

typealias Undefined = Nothing

// ===============================
// Simple types and constrained types related to the OrderTaking domain.
//
// E.g. Single case discriminated unions (aka wrappers), enums, etc
// ===============================

/// Constrained to be 50 chars or less, not null
data class String50 internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<String50> = createString(fieldName, ::String50, 50, str)

        fun createOption(fieldName: String, str: String): Result<Option<String50>> = createStringOption(fieldName, ::String50, 50, str)
    }
}

/// An email address
data class EmailAddress internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<EmailAddress> = createLike(fieldName, ::EmailAddress, ".+@.+", str)
    }
}

/// A zip code
data class ZipCode(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<ZipCode> = createLike(fieldName, ::ZipCode, """\d{5}""", str)
    }
}

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
data class OrderId(val value: Int)

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
data class OrderLineId(val value: Int)

/// A ProductCode is either a Widget or a Gizmo
sealed class ProductCode {
    /// The codes for Widgets start with a "W" and then four digits
    data class WidgetCode(val value: String) : ProductCode()

    /// The codes for Gizmos start with a "G" and then three digits.
    data class GizmoCode(val value: String)  : ProductCode()
}

sealed class OrderQuantity {

    /// Constrained to be a integer between 1 and 1000
    data class UnitQuantity(val value: Int) : OrderQuantity()

    /// Constrained to be a decimal between 0.05 and 100.00
    data class KilogramQuantity(val value: BigDecimal) : OrderQuantity()
}

/// Constrained to be a decimal between 0.0 and 1000.00
data class Price(val value: BigDecimal)

/// Constrained to be a decimal between 0.0 and 10000.00
data class BillingAmount(val value: BigDecimal)

data class PdfAttachment(val name: String, val bytes: ByteArray)



