package com.sforca.rinha_backend.repository.client.postgres

import io.vertx.core.Vertx
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions

private val connectionOptions: PgConnectOptions = PgConnectOptions()
  .setPort(5432)
  .setHost("localhost")
  .setDatabase("rinha")
  .setUser("admin")
  .setPassword("password")

val poolOptions: PoolOptions = PoolOptions()
  .setMaxSize(5)

val pool: Pool = PgBuilder
  .pool()
  .with(poolOptions)
  .connectingTo(connectionOptions)
  .using(Vertx.vertx())
  .build()
