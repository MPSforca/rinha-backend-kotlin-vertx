package com.sforca.rinha_backend.repository.client

import com.sforca.rinha_backend.repository.client.postgres.pool
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient


interface ClientsRepository {

    fun getLastTransactions(clientId: Long, limit: Int, client: SqlClient = pool): Future<List<Transaction>>

    fun getBalanceForUpdate(clientId: Long, client: SqlClient = pool): Future<Balance>

    fun getBalance(clientId: Long, client: SqlClient = pool): Future<Balance>

    fun saveTransaction(transaction: Transaction, newClientBalance: Long, client: SqlClient = pool): Future<Unit>
}
