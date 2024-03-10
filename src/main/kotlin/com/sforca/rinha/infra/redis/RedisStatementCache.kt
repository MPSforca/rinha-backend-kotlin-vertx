package com.sforca.rinha.infra.redis

import com.sforca.rinha.core.entity.Statement
import com.sforca.rinha.core.entity.StatementBalance
import com.sforca.rinha.core.entity.StatementTransaction
import com.sforca.rinha.core.wrapper.StatementCache
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.redis.client.RedisAPI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RedisStatementCache(
    private val redisAPI: RedisAPI,
) : StatementCache {
    private val localDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun get(clientId: Long): Future<Statement?> = redisAPI.get(clientId.toString()).map { it?.toString()?.toStatement() }

    override fun save(statement: Statement) {
        redisAPI.set(listOf(statement.clientId.toString(), statement.toJson()))
    }

    override fun invalidate(clientId: Long) {
        redisAPI.del(listOf(clientId.toString()))
    }

    private fun String.toStatement(): Statement =
        with(JsonObject(this)) {
            Statement(
                clientId = getLong("clientId"),
                balance =
                    with(getJsonObject("balance")) {
                        StatementBalance(
                            value = getLong("value"),
                            limit = getLong("limit"),
                            checkDate = LocalDateTime.parse(getString("checkDate"), localDateTimeFormat),
                        )
                    },
                lastTransactions =
                    getJsonArray("lastTransactions").map { it as JsonObject }.map {
                        StatementTransaction(
                            value = it.getLong("value"),
                            type = it.getString("type").first(),
                            description = it.getString("description"),
                            carriedOutAt = LocalDateTime.parse(it.getString("carriedOutAt"), localDateTimeFormat),
                        )
                    },
            )
        }

    private fun Statement.toJson(): String =
        json {
            obj(
                "clientId" to clientId,
                "balance" to
                    obj(
                        "value" to balance.value,
                        "limit" to balance.limit,
                        "checkDate" to balance.checkDate.format(localDateTimeFormat),
                    ),
                "lastTransactions" to
                    lastTransactions.map {
                        obj(
                            "value" to it.value,
                            "type" to it.type.toString(),
                            "description" to it.description,
                            "carriedOutAt" to it.carriedOutAt.format(localDateTimeFormat),
                        )
                    },
            )
        }.encode()
}
