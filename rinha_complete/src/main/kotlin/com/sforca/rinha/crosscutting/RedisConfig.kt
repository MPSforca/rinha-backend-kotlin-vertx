package com.sforca.rinha.crosscutting

import io.vertx.core.Vertx
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisOptions

private val redisHost by lazy { System.getenv("REDIS_HOST") ?: "localhost" }
private val redisPassword by lazy { System.getenv("REDIS_PASSWORD") ?: "password" }
private val redisPort by lazy { System.getenv("REDIS_PORT")?.toInt() ?: 6379 }

fun redis(vertx: Vertx): Redis =
    Redis.createClient(
        vertx,
        RedisOptions()
            .addConnectionString("redis://:$redisPassword@$redisHost:$redisPort")
            .setMaxPoolSize(6)
            .setMaxPoolWaiting(16),
    )
