package com.sforca.rinha.core.input

data class SaveTransactionInput(
    val clientId: Long,
    val value: Long,
    val type: Char,
    val description: String,
)
