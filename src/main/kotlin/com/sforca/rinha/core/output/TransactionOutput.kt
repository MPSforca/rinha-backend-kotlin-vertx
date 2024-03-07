package com.sforca.rinha.core.output

import java.time.LocalDateTime

data class TransactionOutput(
    val clientId: Long,
    val value: Long,
    val type: Char,
    val description: String,
    val carriedOutAt: LocalDateTime,
)
