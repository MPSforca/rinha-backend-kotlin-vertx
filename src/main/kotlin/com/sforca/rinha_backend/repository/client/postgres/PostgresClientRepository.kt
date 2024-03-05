package com.sforca.rinha_backend.repository.client.postgres

import com.sforca.rinha_backend.core.exception.ClientIdNotFoundException
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.ClientsRepository
import com.sforca.rinha_backend.repository.client.Transaction
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

class PostgresClientRepository : ClientsRepository {

    override fun getLastTransactions(clientId: Long, limit: Int, client: SqlClient): Future<List<Transaction>> =
        client.preparedQuery(
            """
                SELECT value, type, description, carried_out_at FROM transactions
                WHERE client_id = $1
                ORDER BY carried_out_at DESC
                LIMIT $2
          """.trimIndent()
        )
            .execute(Tuple.of(clientId, limit))
            .map {
                it.map {
                    Transaction(
                        clientId = clientId,
                        value = it.getLong("value"),
                        type = it.getString("type").first(),
                        description = it.getString("description"),
                        carriedOutAt = it.getLocalDateTime("carried_out_at")
                    )
                }
            }
            .onFailure { println("Failed to get the last transactions. Cause: ${it.message}") }

    override fun getBalanceForUpdate(clientId: Long, client: SqlClient): Future<Balance> =
        client
            .preparedQuery(
                """
        SELECT balance_limit, current_balance FROM statements WHERE client_id = $1 FOR UPDATE
        """.trimIndent()
            )
            .execute(Tuple.of(clientId))
            .map {
                if (it.size() == 0) {
                    throw ClientIdNotFoundException()
                }
                val row = it.first()
                return@map Balance(
                    value = row.getLong("current_balance"),
                    limit = row.getLong("balance_limit")
                )
            }
            .onFailure { println("Couldn't get the balance from repository. Cause: ${it.cause}") }

    override fun getBalance(clientId: Long, client: SqlClient): Future<Balance> =
        client
            .preparedQuery(
                """
        SELECT balance_limit, current_balance FROM statements WHERE client_id = $1
        """.trimIndent()
            )
            .execute(Tuple.of(clientId))
            .map {
                if (it.size() == 0) {
                    throw ClientIdNotFoundException()
                }
                val row = it.first()
                return@map Balance(
                    value = row.getLong("current_balance"),
                    limit = row.getLong("balance_limit")
                )
            }
            .onFailure { println("Couldn't get the balance from repository. Cause: ${it.cause}") }


    override fun saveTransaction(transaction: Transaction, newClientBalance: Long, client: SqlClient): Future<Unit> =
        client
            .preparedQuery("CALL save_transaction($1, $2, $3, $4, $5, $6)")
            .execute(
                Tuple.of(
                    transaction.clientId,
                    newClientBalance,
                    transaction.value,
                    transaction.type.toString(),
                    transaction.description,
                    transaction.carriedOutAt
                )
            )
            .map { }
            .onSuccess { println("Transaction successfully saved!") }
            .onFailure { println("Failed to save the transaction. Cause: ${it.message}") }
}
