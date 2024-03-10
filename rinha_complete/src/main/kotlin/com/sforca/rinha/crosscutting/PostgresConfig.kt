package com.sforca.rinha.crosscutting

import io.vertx.core.Vertx
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions

private val connectionOptions: PgConnectOptions by lazy {
    PgConnectOptions()
        .setPort(System.getenv("DB_PORT")?.toInt() ?: 5432)
        .setHost(System.getenv("DB_HOST") ?: "localhost")
        .setDatabase(System.getenv("DB_NAME") ?: "rinha")
        .setUser(System.getenv("DB_USER") ?: "admin")
        .setPassword(System.getenv("DB_PASSWORD") ?: "password")
}

private val poolOptions: PoolOptions by lazy {
    PoolOptions()
        .setMaxSize(6)
}

fun pool(vertx: Vertx): Pool =
    PgBuilder
        .pool()
        .with(poolOptions)
        .connectingTo(connectionOptions)
        .using(vertx)
        .build()
