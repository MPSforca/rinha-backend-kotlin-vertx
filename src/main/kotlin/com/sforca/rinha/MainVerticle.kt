package com.sforca.rinha

import com.sforca.rinha.core.GetStatementUseCase
import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.http.ApiRouter
import com.sforca.rinha.repository.client.ClientsRepository
import com.sforca.rinha.repository.client.cache.RedisStatementCache
import com.sforca.rinha.repository.client.cache.StatementCache
import com.sforca.rinha.repository.client.postgres.PostgresClientRepository
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions

class MainVerticle : AbstractVerticle() {
    private val connectionOptions: PgConnectOptions =
        PgConnectOptions()
            .setPort(5432)
            .setHost(System.getenv("DB_HOST") ?: "localhost")
            .setDatabase("rinha")
            .setUser("admin")
            .setPassword("password")

    private val poolOptions: PoolOptions =
        PoolOptions()
            .setMaxSize(6)

    private val pool: Pool by lazy {
        PgBuilder
            .pool()
            .with(poolOptions)
            .connectingTo(connectionOptions)
            .using(vertx)
            .build()
    }

    override fun start(startPromise: Promise<Void>) {
        val clientsRepository: ClientsRepository = PostgresClientRepository()
        val statementCache: StatementCache = RedisStatementCache(clientsRepository, pool)
        val saveTransactionUseCase = SaveTransactionUseCase(statementCache, pool)
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
                    startPromise.fail(http.cause())
                }
            }
    }
}
