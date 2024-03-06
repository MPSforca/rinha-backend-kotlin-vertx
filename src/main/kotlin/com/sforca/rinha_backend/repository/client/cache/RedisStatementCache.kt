package com.sforca.rinha_backend.repository.client.cache

import com.sforca.rinha_backend.core.output.BalanceOutput
import com.sforca.rinha_backend.core.output.GetStatementOutput
import com.sforca.rinha_backend.core.output.TransactionOutput
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.ClientsRepository
import com.sforca.rinha_backend.repository.client.Transaction
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.redis.client.RedisAPI
import io.vertx.sqlclient.SqlClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RedisStatementCache(
    private val clientsRepository: ClientsRepository,
    private val redisApi: RedisAPI,
    private val client: SqlClient
) : StatementCache {

    private val localDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun get(clientId: Long): Future<GetStatementOutput> =
        redisApi.get(clientId.toString())
            .flatMap { response ->
                return@flatMap response?.toString()?.toGetStatementOutput()?.let { Future.succeededFuture(it) }
                    ?: run {
                        statementFromRepository(clientId)
                            .onSuccess {
                                redisApi.set(listOf(clientId.toString(), it.toJson()))
                            }
                    }
            }


    private fun String.toGetStatementOutput(): GetStatementOutput = with(JsonObject(this)) {
        GetStatementOutput(
            clientId = getLong("clientId"),
            balance = with(getJsonObject("balance")) {
                BalanceOutput(
                    value = getLong("value"),
                    limit = getLong("limit")
                )
            },
            checkDate = LocalDateTime.parse(getString("checkDate"), localDateTimeFormat),
            lastTransactions = getJsonArray("lastTransactions").map { it as JsonObject }.map {
                TransactionOutput(
                    clientId = it.getLong("clientId"),
                    value = it.getLong("value"),
                    type = it.getString("type").first(),
                    description = it.getString("description"),
                    carriedOutAt = LocalDateTime.parse(it.getString("carriedOutAt"), localDateTimeFormat)
                )
            }
        )
    }

    private fun GetStatementOutput.toJson(): String = JsonObject()
        .put("clientId", clientId)
        .put("balance", JsonObject().put("value", balance.value).put("limit", balance.limit))
        .put("checkDate", checkDate.format(localDateTimeFormat))
        .put(
            "lastTransactions", lastTransactions.map { transaction ->
                JsonObject()
                    .put("clientId", transaction.clientId)
                    .put("value", transaction.value)
                    .put("type", transaction.type.toString())
                    .put("description", transaction.description)
                    .put("carriedOutAt", transaction.carriedOutAt.format(localDateTimeFormat))
            }
        )
        .encode()

    override fun invalidate(clientId: Long) {
        redisApi.del(listOf(clientId.toString()))
    }

    private fun statementFromRepository(clientId: Long): Future<GetStatementOutput> = Future.all(
        clientsRepository.getBalance(clientId, client),
        clientsRepository.getLastTransactions(clientId, 10, client)
    ).map {
        val balance: Balance = it.resultAt(0)
        val transactions: List<Transaction> = it.resultAt(1)
        return@map GetStatementOutput(
            clientId = clientId,
            balance = BalanceOutput(
                value = balance.value,
                limit = balance.limit
            ),
            checkDate = LocalDateTime.now(),
            lastTransactions = transactions.map { transaction ->
                TransactionOutput(
                    clientId = transaction.clientId,
                    value = transaction.value,
                    type = transaction.type,
                    description = transaction.description,
                    carriedOutAt = transaction.carriedOutAt
                )
            }
        )
    }
}