package com.sforca.rinha.infra.postgres

import com.sforca.rinha.core.entity.Transaction
import com.sforca.rinha.core.repository.TransactionRepository
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

class PostgresTransactionRepository(
    private val pool: Pool,
) : TransactionRepository {
    override fun getLastTransactions(
        clientId: Long,
        limit: Int,
    ): Future<List<Transaction>> =
        pool.preparedQuery(GET_LAST_TRANSACTIONS_QUERY)
            .execute(Tuple.of(clientId, limit))
            .map {
                it.map {
                    Transaction(
                        clientId = clientId,
                        value = it.getLong("value"),
                        type = it.getString("type").first(),
                        description = it.getString("description"),
                        carriedOutAt = it.getLocalDateTime("carried_out_at"),
                    )
                }
            }
            .onFailure { println("Failed to get the last transactions. Cause: ${it.message}") }

    override fun saveTransactionTx(
        transaction: Transaction,
        transactionConn: SqlClient,
    ): Future<Unit> =
        transactionConn
            .preparedQuery(SAVE_TRANSACTION_QUERY)
            .execute(
                Tuple.of(
                    transaction.clientId,
                    transaction.value,
                    transaction.type.toString(),
                    transaction.description,
                    transaction.carriedOutAt,
                ),
            )
            .map { }
            .onFailure { println("Failed to save the transaction. Cause: ${it.message}") }

    private companion object {
        const val SAVE_TRANSACTION_QUERY =
            "INSERT INTO transactions(client_id, value, type, description, carried_out_at) VALUES ($1, $2, $3, $4, $5)"
        const val GET_LAST_TRANSACTIONS_QUERY = """
            SELECT value, type, description, carried_out_at FROM transactions
            WHERE client_id = $1
            ORDER BY carried_out_at DESC
            LIMIT $2
            """
    }
}
