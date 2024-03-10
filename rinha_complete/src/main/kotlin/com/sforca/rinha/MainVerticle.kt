package com.sforca.rinha

import com.sforca.rinha.core.wrapper.GetStatementWithCacheWrapper
import com.sforca.rinha.core.wrapper.SaveTransactionWithCacheWrapper
import com.sforca.rinha.crosscutting.pool
import com.sforca.rinha.crosscutting.redis
import com.sforca.rinha.http.HttpServer
import com.sforca.rinha.infra.postgres.PostgresBalanceRepository
import com.sforca.rinha.infra.postgres.PostgresTransactionRepository
import com.sforca.rinha.infra.redis.RedisStatementCache
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.VertxPrometheusOptions
import io.vertx.redis.client.RedisAPI

class MainVerticle : AbstractVerticle() {
    private val appVertx: Vertx =
        Vertx.vertx(
            VertxOptions().setMetricsOptions(
                MicrometerMetricsOptions()
                    .setPrometheusOptions(VertxPrometheusOptions().setEnabled(true)).setEnabled(true)
                    .setJvmMetricsEnabled(true),
            ),
        )

    override fun start(startPromise: Promise<Void>) {
        val pool = pool(appVertx)
        val balanceRepository = PostgresBalanceRepository(pool)
        val transactionRepository = PostgresTransactionRepository(pool)
        val statementCache = RedisStatementCache(RedisAPI.api(redis(appVertx)))
        val getStatementUseCase = GetStatementWithCacheWrapper(statementCache, balanceRepository, transactionRepository)
        val saveTransactionUseCase =
            SaveTransactionWithCacheWrapper(statementCache, pool, balanceRepository, transactionRepository)

        HttpServer(appVertx, saveTransactionUseCase, getStatementUseCase).start(startPromise, port = 8080)
    }
}
