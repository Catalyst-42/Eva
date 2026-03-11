package com.grace.eva

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform