package com.sforca.rinha_backend.repository.client

import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient


interface ClientsRepository {

    fun getLastTransactions(clientId: Long, limit: Int, client: SqlClient): Future<List<Transaction>>

    fun getBalanceForUpdate(clientId: Long, client: SqlClient): Future<Balance>

    fun getBalance(clientId: Long, client: SqlClient): Future<Balance>

    fun updateBalance(clientId: Long, newBalance: Long, client: SqlClient): Future<Balance>

    fun saveTransaction(transaction: Transaction, client: SqlClient): Future<Unit>
}
