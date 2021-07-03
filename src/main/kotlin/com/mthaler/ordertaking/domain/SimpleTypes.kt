package com.mthaler.ordertaking.domain

import com.mthaler.ordertaking.Option
import com.mthaler.ordertaking.Result
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
data class OrderId internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<OrderId> = createString(fieldName, ::OrderId, 50, str)
    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
data class OrderLineId(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<OrderLineId> = createString(fieldName, ::OrderLineId, 50, str)
    }
}

/// A ProductCode is either a Widget or a Gizmo
sealed class ProductCode {
    /// The codes for Widgets start with a "W" and then four digits
    data class WidgetCode internal constructor(val value: String) : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): Result<WidgetCode> = createLike(fieldName, ::WidgetCode, """W\d{4}""", code)
        }
    }

    /// The codes for Gizmos start with a "G" and then three digits.
    data class GizmoCode internal constructor(val value: String)  : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): Result<GizmoCode> = createLike(fieldName, ::GizmoCode, """G\d{3}""", code)
        }
    }

    companion object {

        operator fun invoke(fieldName: String, code: String): Result<ProductCode> {
            when {
                code.isEmpty() -> {
                    val msg = "$fieldName: Must not be empty"
                    return Result.Error(msg)
                }
                code.startsWith("W") -> {
                    return WidgetCode(fieldName, code)
                }
                code.startsWith("G") -> {
                    return GizmoCode(fieldName, code)
                }
                else -> {
                    val msg =  "$fieldName: Format not recognized '$code'"
                    return Result.Error(msg)
                }
            }
        }
    }
}

sealed class OrderQuantity {

    /// Constrained to be a integer between 1 and 1000
    data class UnitQuantity internal constructor(val value: Int) : OrderQuantity() {

        companion object {
            operator fun invoke(fieldName: String, v: Int): Result<UnitQuantity> = createInt(fieldName, ::UnitQuantity, 1, 1000, v)
        }
    }

    /// Constrained to be a decimal between 0.05 and 100.00
    data class KilogramQuantity internal constructor(val value: Double) : OrderQuantity() {

        companion object {
            operator fun invoke(fieldName: String, v: Double): Result<KilogramQuantity> = createDecimal(fieldName, ::KilogramQuantity, 0.05, 100.0, v)
        }
    }

    companion object {
        operator fun invoke(fieldName: String, productCode: ProductCode, quantity: Number): Result<OrderQuantity> {
            return when(productCode) {
                is ProductCode.WidgetCode -> {
                    UnitQuantity(fieldName, quantity.toInt())
                }
                is ProductCode.GizmoCode -> {
                    KilogramQuantity(fieldName, quantity.toDouble())
                }
            }
        }
    }
}

/// Constrained to be a decimal between 0.0 and 1000.00
data class Price internal constructor(val value: Double) {

    companion object {
        operator fun invoke(v: Double) = createDecimal("Price", ::Price, 0.0, 1000.0, v)

        fun unsafeCreate(v: Double): Price {
            val p = invoke(v)
            return when(p) {
                is Result.Ok -> p.value
                is Result.Error -> throw IllegalArgumentException("Not expecting Price to be out of bounds: ${p.value}")
            }
        }
    }

    operator fun times(value: Double): Result<Price> = invoke(this.value * value)
}

/// Constrained to be a decimal between 0.0 and 10000.00
data class BillingAmount(val value: Double)

data class PdfAttachment(val name: String, val bytes: ByteArray)



