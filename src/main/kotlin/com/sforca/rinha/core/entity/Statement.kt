package com.sforca.rinha.core.entity

import java.time.LocalDateTime

data class Statement(
    val clientId: Long,
    val balance: StatementBalance,
    val lastTransactions: List<StatementTransaction>,
)

data class StatementBalance(
    val checkDate: LocalDateTime,
    val value: Long,
    val limit: Long,
)

data class StatementTransaction(
    val value: Long,
    val type: Char,
    val description: String,
    val carriedOutAt: LocalDateTime,
)
