package com.sforca.rinha.core.output

import java.time.LocalDateTime

data class GetStatementOutput(
    val clientId: Long,
    val balance: BalanceOutput,
    val checkDate: LocalDateTime,
    val lastTransactions: List<TransactionOutput>,
)
