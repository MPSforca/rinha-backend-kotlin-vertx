package com.sforca.rinha.core.repository

import com.sforca.rinha.core.entity.Transaction
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient

interface TransactionRepository {
    fun getLastTransactions(
        clientId: Long,
        limit: Int,
    ): Future<List<Transaction>>

    fun saveTransactionTx(
        transaction: Transaction,
        transactionConn: SqlClient,
    ): Future<Unit>
}
