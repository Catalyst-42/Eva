package com.grace.eva.util

import androidx.compose.ui.graphics.Color

/**
 * Parses a hex color string to Compose Color.
 *
 * Supports formats:
 * - #RRGGBB
 * - RRGGBB
 *
 * @param hex The hex color string to parse
 * @return Parsed Color or null if string cannot be parsed
 */
fun parseColor(hex: String): Color? {
    val cleanHex = hex.replace("#", "").trim()
    if (cleanHex.length != 6) return null
    val alphaFF = 0xFF000000

    return try {
        Color(alphaFF or cleanHex.toLong(16))
    } catch (e: NumberFormatException) {
        null // Invalid color
    }
}