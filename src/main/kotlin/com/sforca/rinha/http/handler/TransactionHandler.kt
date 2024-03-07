package com.sforca.rinha.http.handler

import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.core.input.SaveTransactionInput
import com.sforca.rinha.core.output.SaveTransactionOutput
import com.sforca.rinha.http.APPLICATION_JSON
import com.sforca.rinha.http.CONTENT_TYPE_HEADER
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class TransactionHandler(
    private val saveTransaction: SaveTransactionUseCase,
) {
    fun save(rc: RoutingContext): Future<SaveTransactionOutput> {
        val input: SaveTransactionInput =
            rc.body().asJsonObject().let {
                SaveTransactionInput(
                    clientId = rc.pathParam("id").toLong(),
                    value = it.getLong("valor"),
                    type = it.getString("tipo").first(),
                    description = it.getString("descricao"),
                )
            }
        return saveTransaction(input)
            .onSuccess { saveTransactionSuccessResponse(rc, it) }
            .onFailure { rc.fail(it) }
    }

    private fun saveTransactionSuccessResponse(
        rc: RoutingContext,
        output: SaveTransactionOutput,
    ) = rc.response()
        .setStatusCode(200)
        .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
        .end(
            JsonObject()
                .put("limite", output.limit)
                .put("saldo", output.value)
                .encode(),
        )
}
