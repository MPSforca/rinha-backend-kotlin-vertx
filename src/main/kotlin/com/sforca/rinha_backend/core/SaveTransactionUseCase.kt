package com.sforca.rinha_backend.core

import com.sforca.rinha_backend.core.exception.InconsistentTransactionValueException
import com.sforca.rinha_backend.core.exception.InvalidDescriptionLengthException
import com.sforca.rinha_backend.core.input.SaveTransactionInput
import com.sforca.rinha_backend.core.input.TransactionType
import com.sforca.rinha_backend.core.output.SaveTransactionOutput
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.Transaction
import com.sforca.rinha_backend.repository.client.postgres.pool
import com.sforca.rinha_backend.repository.clientsRepository
import io.vertx.core.Future
import java.lang.RuntimeException
import java.time.LocalDateTime

class SaveTransactionUseCase {

  operator fun invoke(input: SaveTransactionInput): Future<SaveTransactionOutput> {
    if (input.description.length !in 1..10) {
      return Future.failedFuture(InvalidDescriptionLengthException())
    }
    return pool.connection
      .flatMap { conn ->
        conn.begin()
          .compose { tx ->
            clientsRepository.getBalanceForUpdate(input.clientId, conn)
              .map {
                if (input.type == TransactionType.DEBIT) {
                  if (it.value - input.value < it.limit * -1) {
                   throw InconsistentTransactionValueException()
                  }
                }
                it
              }
              .flatMap {
                val newBalance =
                  if (input.type == TransactionType.DEBIT) it.value - input.value else it.value + input.value
                Future.all(
                  clientsRepository.saveTransaction(
                    transaction = Transaction(
                      clientId = input.clientId,
                      value = input.value,
                      type = if (input.type == TransactionType.DEBIT) 'd' else 'c',
                      description = input.description,
                      carriedOutAt = LocalDateTime.now()
                    ),
                    client = conn
                  ),
                  clientsRepository.updateBalance(input.clientId, newBalance, client = conn)
                )
              }
              .onSuccess { tx.commit() }
              .map {
                val balance: Balance = it.resultAt(1)
                return@map SaveTransactionOutput(
                  limit = balance.limit,
                  value = balance.value
                )
              }
          }
          .eventually { v -> conn.close() }
          .onSuccess { println("Transaction saved successfully for client ${input.clientId}") }
          .onFailure { println("Transaction failed. Cause: ${it.message}") }
      }
  }
}
