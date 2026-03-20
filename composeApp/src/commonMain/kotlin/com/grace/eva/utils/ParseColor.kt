package com.grace.eva.utils

import androidx.compose.ui.graphics.Color

/**
 * Parses a hex color string to Compose Color.
 *
 * Supports formats:
 * - "#RRGGBB" (e.g., "#FF0000" for red)
 * - "RRGGBB" (e.g., "00FF00" for green)
 *
 * @param hex The hex color string to parse
 * @return Parsed Color or null if string cannot be parsed
 */
fun parseColor(hex: String): Color? {
    val cleanHex = hex.replace("#", "").trim()
    if (cleanHex.length != 6) return null

    return try {
        Color(0xFF000000 or cleanHex.toLong(16))
    } catch (e: NumberFormatException) {
        null
    }
}
