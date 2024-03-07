package com.sforca.rinha.repository.client.cache

import com.sforca.rinha.core.output.BalanceOutput
import com.sforca.rinha.core.output.GetStatementOutput
import com.sforca.rinha.core.output.TransactionOutput
import com.sforca.rinha.repository.client.Balance
import com.sforca.rinha.repository.client.ClientsRepository
import com.sforca.rinha.repository.client.Transaction
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import java.time.LocalDateTime

class RedisStatementCache(
    private val clientsRepository: ClientsRepository,
    private val client: SqlClient,
) : StatementCache {
    override fun get(clientId: Long): Future<GetStatementOutput> = statementFromRepository(clientId)

    override fun invalidate(clientId: Long) {
    }

    private fun statementFromRepository(clientId: Long): Future<GetStatementOutput> =
        Future.all(
            clientsRepository.getBalance(clientId, client),
            clientsRepository.getLastTransactions(clientId, 10, client),
        ).map {
            val balance: Balance = it.resultAt(0)
            val transactions: List<Transaction> = it.resultAt(1)
            return@map GetStatementOutput(
                clientId = clientId,
                balance =
                    BalanceOutput(
                        value = balance.value,
                        limit = balance.limit,
                    ),
                checkDate = LocalDateTime.now(),
                lastTransactions =
                    transactions.map { transaction ->
                        TransactionOutput(
                            clientId = transaction.clientId,
                            value = transaction.value,
                            type = transaction.type,
                            description = transaction.description,
                            carriedOutAt = transaction.carriedOutAt,
                        )
                    },
            )
        }
}
