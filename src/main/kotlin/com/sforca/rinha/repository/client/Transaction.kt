package com.sforca.rinha.repository.client

import java.time.LocalDateTime

data class Transaction(
    val clientId: Long,
    val value: Long,
    val type: Char,
    val description: String,
    val carriedOutAt: LocalDateTime,
)
