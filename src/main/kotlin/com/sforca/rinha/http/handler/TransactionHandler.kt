package com.sforca.rinha.http.handler

import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.core.entity.Balance
import com.sforca.rinha.core.input.SaveTransactionInput
import com.sforca.rinha.http.APPLICATION_JSON
import com.sforca.rinha.http.CONTENT_TYPE_HEADER
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class TransactionHandler(
    private val saveTransaction: SaveTransactionUseCase,
) {
    fun save(rc: RoutingContext): Future<Balance> {
        val input = rc.body().asJsonObject().toSaveTransactionInput(rc.pathParam("id").toLong())
        return saveTransaction(input)
            .onSuccess { it.toSuccessResponse(rc) }
            .onFailure { rc.fail(it) }
    }

    private fun JsonObject.toSaveTransactionInput(clientId: Long): SaveTransactionInput =
        SaveTransactionInput(
            clientId = clientId,
            value = getLong("valor"),
            type = getString("tipo").first(),
            description = getString("descricao"),
        )

    private fun Balance.toSuccessResponse(rc: RoutingContext) =
        rc.response()
            .setStatusCode(200)
            .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
            .end(this.toJson().encode())

    private fun Balance.toJson(): JsonObject =
        json {
            obj(
                "limite" to limit,
                "saldo" to value,
            )
        }
}
