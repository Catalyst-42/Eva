package com.grace.eva.util

import kotlin.math.pow

/**
 * Formats float with given decimal places.
 *
 * @param value The float to format
 * @param decimals Number of decimal places
 */
fun formatFloat(value: Float, decimals: Int): String {
    val integerPart = value.toInt()
    val fractionalPart = ((value - integerPart) * 10.0.pow(decimals)).toInt()
    val fractionalStr = fractionalPart.toString().padStart(decimals, '0')

    return if (decimals > 0) {
        "$integerPart.$fractionalStr"
    } else {
        integerPart.toString()
    }
}
