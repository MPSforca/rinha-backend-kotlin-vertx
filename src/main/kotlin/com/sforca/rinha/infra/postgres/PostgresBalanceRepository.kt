package com.sforca.rinha.infra.postgres

import com.sforca.rinha.core.entity.Balance
import com.sforca.rinha.core.exception.ClientIdNotFoundException
import com.sforca.rinha.core.repository.BalanceRepository
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

class PostgresBalanceRepository(
    private val pool: Pool,
) : BalanceRepository {
    override fun getBalance(clientId: Long): Future<Balance> =
        pool
            .preparedQuery(GET_BALANCE_QUERY)
            .execute(Tuple.of(clientId))
            .map {
                if (it.size() == 0) {
                    throw ClientIdNotFoundException()
                }
                val row = it.first()
                return@map Balance(
                    value = row.getLong("current_balance"),
                    limit = row.getLong("balance_limit"),
                )
            }
            .onFailure { println("Couldn't get the balance from repository. Cause: ${it.cause}") }

    override fun getBalanceForUpdateTx(
        clientId: Long,
        transactionConn: SqlClient,
    ): Future<Balance> =
        transactionConn
            .preparedQuery(GET_BALANCE_FOR_UPDATE_QUERY)
            .execute(Tuple.of(clientId))
            .map {
                if (it.size() == 0) {
                    throw ClientIdNotFoundException()
                }
                val row = it.first()
                return@map Balance(
                    value = row.getLong("current_balance"),
                    limit = row.getLong("balance_limit"),
                )
            }
            .onFailure { println("Couldn't get the balance from repository. Cause: ${it.cause}") }

    override fun updateBalanceTx(
        clientId: Long,
        newBalance: Long,
        transactionConn: SqlClient,
    ): Future<Balance> =
        transactionConn
            .preparedQuery(UPDATE_BALANCE_QUERY)
            .execute(Tuple.of(newBalance, clientId))
            .map {
                val row = it.first()
                return@map Balance(
                    value = row.getLong("current_balance"),
                    limit = row.getLong("balance_limit"),
                )
            }
            .onFailure { println("Failed to update the balance of user $clientId. Cause: ${it.message}") }

    private companion object {
        const val GET_BALANCE_QUERY = "SELECT balance_limit, current_balance FROM statements WHERE client_id = $1"
        const val GET_BALANCE_FOR_UPDATE_QUERY =
            "SELECT balance_limit, current_balance FROM statements WHERE client_id = $1 FOR UPDATE"
        const val UPDATE_BALANCE_QUERY =
            "UPDATE statements SET current_balance = $1 WHERE client_id = $2 RETURNING balance_limit, current_balance"
    }
}
