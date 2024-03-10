package com.sforca.rinha.core.repository

import com.sforca.rinha.core.entity.Balance
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient

interface BalanceRepository {
    fun getBalance(clientId: Long): Future<Balance>

    fun getBalanceForUpdateTx(
        clientId: Long,
        transactionConn: SqlClient,
    ): Future<Balance>

    fun updateBalanceTx(
        clientId: Long,
        newBalance: Long,
        transactionConn: SqlClient,
    ): Future<Balance>
}
