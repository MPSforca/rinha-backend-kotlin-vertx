package com.sforca.rinha.core

import com.sforca.rinha.core.entity.*
import com.sforca.rinha.core.repository.BalanceRepository
import com.sforca.rinha.core.repository.TransactionRepository
import io.vertx.core.Future
import java.time.LocalDateTime

open class GetStatementUseCase(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
) : UseCase<Long, Future<Statement>> {
    override operator fun invoke(input: Long): Future<Statement> =
        Future.all(
            balanceRepository.getBalance(clientId = input),
            transactionRepository.getLastTransactions(clientId = input, limit = 10),
        ).map {
            val balance: Balance = it.resultAt(0)
            val transactions: List<Transaction> = it.resultAt(1)
            return@map Statement(
                clientId = input,
                balance =
                    StatementBalance(
                        value = balance.value,
                        limit = balance.limit,
                        checkDate = LocalDateTime.now(),
                    ),
                lastTransactions =
                    transactions.map { transaction ->
                        StatementTransaction(
                            value = transaction.value,
                            type = transaction.type,
                            description = transaction.description,
                            carriedOutAt = transaction.carriedOutAt,
                        )
                    },
            )
        }
}
