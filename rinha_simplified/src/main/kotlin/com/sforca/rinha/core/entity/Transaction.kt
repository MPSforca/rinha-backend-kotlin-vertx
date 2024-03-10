package com.sforca.rinha.core.entity

import java.time.LocalDateTime

data class Transaction(
    val clientId: Long,
    val value: Long,
    val type: Char,
    val description: String,
    val carriedOutAt: LocalDateTime,
)
