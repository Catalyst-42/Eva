package com.grace.eva.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Formats float with given decimal places.
 *
 * @param value The float to format
 * @param decimals Number of decimal places
 */
fun formatFloat(value: Float, decimals: Int): String {
    val scale = 10.0.pow(decimals).toLong()
    val roundedValue = (value.toDouble() * scale).roundToLong()
    val absoluteValue = abs(roundedValue)
    val integerPart = absoluteValue / scale
    val fractionalPart = absoluteValue % scale
    val fractionalStr = fractionalPart.toString().padStart(decimals, '0')
    val sign = if (roundedValue < 0) "-" else ""

    return if (decimals > 0) {
        "$sign$integerPart.$fractionalStr"
    } else {
        "$sign$integerPart"
    }
}
