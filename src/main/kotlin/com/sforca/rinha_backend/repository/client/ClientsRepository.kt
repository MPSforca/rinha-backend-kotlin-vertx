package com.sforca.rinha_backend.repository.client

import com.sforca.rinha_backend.repository.client.postgres.pool
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient


interface ClientsRepository {

  fun getLastTransactions(clientId: Long, limit: Int): Future<List<Transaction>>

  fun getBalanceForUpdate(clientId: Long, client: SqlClient = pool): Future<Balance>

  fun updateBalance(clientId: Long, newBalance: Long, client: SqlClient = pool): Future<Balance>

  fun saveTransaction(transaction: Transaction, client: SqlClient = pool): Future<Unit>
}
