package com.sforca.rinha_backend.core.input

data class SaveTransactionInput(
  val clientId: Long,
  val value: Long,
  val type: Char,
  val description: String,
)
