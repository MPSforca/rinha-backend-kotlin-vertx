package com.sforca.rinha_backend.core

import com.sforca.rinha_backend.core.exception.InconsistentTransactionValueException
import com.sforca.rinha_backend.core.input.SaveTransactionInput
import com.sforca.rinha_backend.core.output.SaveTransactionOutput
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.Transaction
import com.sforca.rinha_backend.repository.client.cache.StatementCache
import com.sforca.rinha_backend.repository.client.postgres.pool
import com.sforca.rinha_backend.repository.clientsRepository
import io.vertx.core.Future
import java.time.LocalDateTime

class SaveTransactionUseCase(private val cache: StatementCache) {

    operator fun invoke(newTransaction: SaveTransactionInput): Future<SaveTransactionOutput> = pool.connection
        .flatMap { conn ->
            conn.begin()
                .compose { tx ->
                    clientsRepository
                        .getBalanceForUpdate(newTransaction.clientId, conn)
                        .map {
                            if (isDebitTransaction(newTransaction.type)) {
                                checkTransactionConsistency(newTransaction.value, it.limit, it.value)
                            }
                            it
                        }
                        .flatMap { currentBalance ->
                            val newBalance =
                                calculateNewBalance(newTransaction.type, currentBalance.value, newTransaction.value)
                            Future.all(
                                clientsRepository.saveTransaction(
                                    transaction = Transaction(
                                        clientId = newTransaction.clientId,
                                        value = newTransaction.value,
                                        type = newTransaction.type,
                                        description = newTransaction.description,
                                        carriedOutAt = LocalDateTime.now()
                                    ),
                                    client = conn
                                ),
                                clientsRepository.updateBalance(newTransaction.clientId, newBalance, client = conn)
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
                        .eventually { v -> conn.close() }
                }
                .onSuccess { cache.invalidate(newTransaction.clientId) }
        }

    private fun isDebitTransaction(type: Char) = type == 'd'

    private fun calculateNewBalance(type: Char, currentBalanceValue: Long, transactionBalanceValue: Long) =
        if (isDebitTransaction(type)) currentBalanceValue - transactionBalanceValue else currentBalanceValue + transactionBalanceValue

    private fun checkTransactionConsistency(newTransactionValue: Long, currentLimit: Long, currentBalanceValue: Long) {
        if (currentBalanceValue - newTransactionValue < currentLimit * -1) {
            throw InconsistentTransactionValueException()
        }
    }

}