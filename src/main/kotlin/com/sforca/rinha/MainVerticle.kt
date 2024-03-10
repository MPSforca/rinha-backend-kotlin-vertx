package com.sforca.rinha

import com.sforca.rinha.core.GetStatementUseCase
import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.crosscutting.pool
import com.sforca.rinha.http.HttpServer
import com.sforca.rinha.infra.postgres.PostgresBalanceRepository
import com.sforca.rinha.infra.postgres.PostgresTransactionRepository
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class MainVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        val pool = pool(vertx)
        val balanceRepository = PostgresBalanceRepository(pool)
        val transactionRepository = PostgresTransactionRepository(pool)

        val saveTransactionUseCase = SaveTransactionUseCase(pool, balanceRepository, transactionRepository)
        val getStatementUseCase = GetStatementUseCase(balanceRepository, transactionRepository)

        HttpServer(vertx, saveTransactionUseCase, getStatementUseCase).start(startPromise, port = 8080)
    }
}
