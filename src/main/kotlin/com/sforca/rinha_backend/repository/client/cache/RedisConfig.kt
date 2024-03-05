package com.sforca.rinha_backend.repository.client.cache

import io.vertx.core.Vertx
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions

private val redisHost = System.getenv("REDIS_HOST") ?: "localhost"

private val redis: Redis = Redis.createClient(
    Vertx.vertx(),
    RedisOptions()
        .addConnectionString("redis://:password@$redisHost:6379")
        .setMaxPoolSize(4)
        .setMaxPoolWaiting(16)
)

val redisApi: RedisAPI = RedisAPI.api(redis)