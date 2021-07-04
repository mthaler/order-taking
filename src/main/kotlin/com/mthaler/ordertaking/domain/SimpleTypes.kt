package com.mthaler.ordertaking.domain

import arrow.core.Option
import arrow.core.Validated
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
        operator fun invoke(fieldName: String, str: String): Validated<String, String50> = createString(fieldName, ::String50, 50, str)

        fun createOption(fieldName: String, str: String): Result<Option<String50>, String> = createStringOption(fieldName, ::String50, 50, str)
    }
}

/// An email address
data class EmailAddress internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<EmailAddress, String> = createLike(fieldName, ::EmailAddress, ".+@.+", str)
    }
}

/// A zip code
data class ZipCode(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Result<ZipCode, String> = createLike(fieldName, ::ZipCode, """\d{5}""", str)
    }
}

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
data class OrderId internal constructor(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Validated<String, OrderId> = createString(fieldName, ::OrderId, 50, str)
    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
data class OrderLineId(val value: String) {

    companion object {
        operator fun invoke(fieldName: String, str: String): Validated<String, OrderLineId> = createString(fieldName, ::OrderLineId, 50, str)
    }
}

/// A ProductCode is either a Widget or a Gizmo
sealed class ProductCode {
    /// The codes for Widgets start with a "W" and then four digits
    data class WidgetCode internal constructor(val value: String) : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): Result<WidgetCode, String> = createLike(fieldName, ::WidgetCode, """W\d{4}""", code)
        }
    }

    /// The codes for Gizmos start with a "G" and then three digits.
    data class GizmoCode internal constructor(val value: String)  : ProductCode() {

        companion object {
            // The codes for Widgets start with a "W" and then four digits
            operator fun invoke(fieldName: String, code: String): Result<GizmoCode, String> = createLike(fieldName, ::GizmoCode, """G\d{3}""", code)
        }
    }

    companion object {

        operator fun invoke(fieldName: String, code: String): Result<ProductCode, String> {
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
            operator fun invoke(fieldName: String, v: Int): Result<UnitQuantity, String> = createInt(fieldName, ::UnitQuantity, 1, 1000, v)
        }
    }

    /// Constrained to be a decimal between 0.05 and 100.00
    data class KilogramQuantity internal constructor(val value: Double) : OrderQuantity() {

        companion object {
            operator fun invoke(fieldName: String, v: Double): Result<KilogramQuantity, String> = createDecimal(fieldName, ::KilogramQuantity, 0.05, 100.0, v)
        }
    }

    companion object {
        operator fun invoke(fieldName: String, productCode: ProductCode, quantity: Number): Result<OrderQuantity, String> {
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
        operator fun invoke(v: Double): Result<Price, String> = createDecimal("Price", ::Price, 0.0, 1000.0, v)

        fun unsafeCreate(v: Double): Price {
            val p = invoke(v)
            return when(p) {
                is Result.Ok -> p.value
                is Result.Error -> throw IllegalArgumentException("Not expecting Price to be out of bounds: ${p.value}")
            }
        }
    }

    operator fun times(value: Double): Result<Price, String> = invoke(this.value * value)
}

/// Constrained to be a decimal between 0.0 and 10000.00
data class BillingAmount internal constructor(val value: Double) {

    companion object {
        operator fun invoke(v: Double): Result<BillingAmount, String> = createDecimal("BillingAmount", ::BillingAmount, 0.0, 10000.0, v)
    }
}

fun List<Price>.sumPrices(): Result<BillingAmount, String> {
    val total = this.map { it.value }.sum()
    return BillingAmount.invoke(total)
}

data class PdfAttachment(val name: String, val bytes: ByteArray)



