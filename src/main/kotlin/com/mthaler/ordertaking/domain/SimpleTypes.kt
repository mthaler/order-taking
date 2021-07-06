package com.mthaler.ordertaking.domain

import arrow.core.Option
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import com.mthaler.ordertaking.validation.*
import java.lang.IllegalArgumentException

typealias Undefined = Nothing

// ===============================
// Simple types and constrained types related to the OrderTaking domain.
//
// E.g. Single case discriminated unions (aka wrappers), enums, etc
// ===============================

/// Constrained to be 50 chars or less, not null
data class String50 internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): ValidatedNel<String, String50> = createString(fieldName, ::String50, 50, str)

        fun createOption(fieldName: String, str: String): ValidatedNel<String, Option<String50>> = createStringOption(fieldName, ::String50, 50, str)
    }
}

/// An email address
data class EmailAddress internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): ValidatedNel<String, EmailAddress> = createLike(fieldName, ::EmailAddress, ".+@.+", str)
    }
}

/// A zip code
data class ZipCode(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): ValidatedNel<String, ZipCode> = createLike(fieldName, ::ZipCode, """\d{5}""", str)
    }
}

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
data class OrderId internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): ValidatedNel<String, OrderId> = createString(fieldName, ::OrderId, 50, str)
    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
data class OrderLineId(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): ValidatedNel<String, OrderLineId> = createString(fieldName, ::OrderLineId, 50, str)
    }
}

/// A ProductCode is either a Widget or a Gizmo
sealed class ProductCode {
    /// The codes for Widgets start with a "W" and then four digits
    data class WidgetCode internal constructor(val value: String) : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): ValidatedNel<String, WidgetCode> = createLike(fieldName, ::WidgetCode, """W\d{4}""", code)
        }
    }

    /// The codes for Gizmos start with a "G" and then three digits.
    data class GizmoCode internal constructor(val value: String)  : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): ValidatedNel<String, GizmoCode> = createLike(fieldName, ::GizmoCode, """G\d{3}""", code)
        }
    }

    companion object {

        operator fun invoke(fieldName: String, code: String): ValidatedNel<String, ProductCode> = when {
            code.isEmpty() -> "$fieldName: Must not be empty".invalidNel()
            code.startsWith("W") -> WidgetCode(fieldName, code)
            code.startsWith("G") -> GizmoCode(fieldName, code)
            else -> "$fieldName: Format not recognized '$code'".invalidNel()
        }
    }
}

sealed class OrderQuantity {

    /// Constrained to be a integer between 1 and 1000
    data class UnitQuantity internal constructor(val value: Int) : OrderQuantity() {

        companion object {
            operator fun invoke(fieldName: String, v: Int): ValidatedNel<String, UnitQuantity> = createInt(fieldName, ::UnitQuantity, 1, 1000, v)
        }
    }

    /// Constrained to be a decimal between 0.05 and 100.00
    data class KilogramQuantity internal constructor(val value: Double) : OrderQuantity() {

        companion object {
            operator fun invoke(fieldName: String, v: Double): ValidatedNel<String, KilogramQuantity> = createDecimal(fieldName, ::KilogramQuantity, 0.05, 100.0, v)
        }
    }

    fun value(): Number = when(this) {
        is UnitQuantity -> value
        is KilogramQuantity -> value
    }

    companion object {
        operator fun invoke(fieldName: String, productCode: ProductCode, quantity: Number): ValidatedNel<String, OrderQuantity> {
            return when(productCode) {
                is ProductCode.WidgetCode -> UnitQuantity(fieldName, quantity.toInt())
                is ProductCode.GizmoCode -> KilogramQuantity(fieldName, quantity.toDouble())
            }
        }
    }
}

/// Constrained to be a decimal between 0.0 and 1000.00
data class Price internal constructor(val value: Double) {

    companion object {
        operator fun invoke(v: Double): ValidatedNel<String, Price> = createDecimal("Price", ::Price, 0.0, 1000.0, v)

        fun unsafeCreate(v: Double): Price {
            val p = invoke(v)
            return when(p) {
                is Validated.Valid -> p.value
                is Validated.Invalid -> throw IllegalArgumentException("Not expecting Price to be out of bounds: ${p.value}")
            }
        }
    }

    operator fun times(value: Double): ValidatedNel<String, Price> = invoke(this.value * value)
}

/// Constrained to be a decimal between 0.0 and 10000.00
data class BillingAmount internal constructor(val value: Double) {

    companion object {
        operator fun invoke(v: Double): ValidatedNel<String, BillingAmount> = createDecimal("BillingAmount", ::BillingAmount, 0.0, 10000.0, v)
    }
}

fun List<Price>.sumPrices(): ValidatedNel<String, BillingAmount> {
    val total = this.map { it.value }.sum()
    return BillingAmount.invoke(total)
}

data class PdfAttachment(val name: String, val bytes: ByteArray)



