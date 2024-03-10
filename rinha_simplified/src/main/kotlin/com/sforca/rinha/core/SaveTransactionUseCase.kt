package com.sforca.rinha.core

import com.sforca.rinha.core.entity.Balance
import com.sforca.rinha.core.entity.Transaction
import com.sforca.rinha.core.exception.InconsistentTransactionValueException
import com.sforca.rinha.core.input.SaveTransactionInput
import com.sforca.rinha.core.repository.BalanceRepository
import com.sforca.rinha.core.repository.TransactionRepository
import com.sforca.rinha.crosscutting.withTransaction
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import java.time.LocalDateTime

open class SaveTransactionUseCase(
    private val pool: Pool,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
) : UseCase<SaveTransactionInput, Future<Balance>> {
    override operator fun invoke(input: SaveTransactionInput): Future<Balance> =
        pool.withTransaction { conn, tx ->
            balanceRepository.getBalanceForUpdateTx(input.clientId, conn)
                .map {
                    return@map it.calculateNewBalance(input).also { newBalance ->
                        if (newBalance.value < newBalance.limit * -1) {
                            throw InconsistentTransactionValueException()
                        }
                    }
                }
                .flatMap { newBalance ->
                    Future.all(
                        transactionRepository.saveTransactionTx(input.toTransaction(), conn),
                        balanceRepository.updateBalanceTx(input.clientId, newBalance.value, conn),
                    ).map { it.resultAt<Balance>(1) }
                }
                .onSuccess { tx.commit() }
        }

    private fun Balance.calculateNewBalance(newTransaction: SaveTransactionInput): Balance {
        val newValue = if (newTransaction.isDebit()) value - newTransaction.value else value + newTransaction.value
        return Balance(
            limit = limit,
            value = newValue,
        )
    }

    private fun SaveTransactionInput.isDebit() = this.type == 'd'

    private fun SaveTransactionInput.toTransaction() =
        Transaction(
            clientId = clientId,
            value = value,
            type = type,
            description = description,
            carriedOutAt = LocalDateTime.now(),
        )
}
