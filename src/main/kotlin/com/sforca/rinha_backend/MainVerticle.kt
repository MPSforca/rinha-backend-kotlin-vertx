package com.sforca.rinha_backend

import com.sforca.rinha_backend.core.GetStatementUseCase
import com.sforca.rinha_backend.core.SaveTransactionUseCase
import com.sforca.rinha_backend.http.ApiRouter
import com.sforca.rinha_backend.repository.client.ClientsRepository
import com.sforca.rinha_backend.repository.client.cache.RedisStatementCache
import com.sforca.rinha_backend.repository.client.cache.StatementCache
import com.sforca.rinha_backend.repository.client.postgres.PostgresClientRepository
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class MainVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        val clientsRepository: ClientsRepository = PostgresClientRepository()
        val statementCache: StatementCache = RedisStatementCache(clientsRepository)
        val saveTransactionUseCase = SaveTransactionUseCase(statementCache)
        val getStatementUseCase = GetStatementUseCase(statementCache)
        val apiRouter = ApiRouter(vertx, saveTransactionUseCase, getStatementUseCase).router()
        val port = 8080
        vertx
            .createHttpServer()
            .requestHandler(apiRouter)
            .listen(port) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    println("HTTP server started on port $port")
                } else {
                    startPromise.fail(http.cause());
                }
            }
    }
}
