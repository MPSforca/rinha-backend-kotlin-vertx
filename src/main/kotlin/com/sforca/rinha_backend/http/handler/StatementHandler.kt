package com.sforca.rinha_backend.http.handler

import com.sforca.rinha_backend.core.GetStatementUseCase
import com.sforca.rinha_backend.core.output.GetStatementOutput
import com.sforca.rinha_backend.http.APPLICATION_JSON
import com.sforca.rinha_backend.http.CONTENT_TYPE_HEADER
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class StatementHandler(
    private val getStatement: GetStatementUseCase
) {

    fun get(rc: RoutingContext): Future<GetStatementOutput> =
        getStatement(clientId = rc.pathParam("id").toLong())
            .onSuccess { getStatementSuccessResponse(rc, it) }
            .onFailure { rc.fail(it) }

    private fun getStatementSuccessResponse(rc: RoutingContext, output: GetStatementOutput) =
        rc.response()
            .setStatusCode(200)
            .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
            .end(
                JsonObject()
                    .put(
                        "saldo",
                        JsonObject()
                            .put("total", output.balance.value)
                            .put("data_extrato", output.checkDate.toString())
                            .put("limite", output.balance.limit)
                    )
                    .put(
                        "ultimas_transacoes", output.lastTransactions.map { transaction ->
                            JsonObject()
                                .put("valor", transaction.value)
                                .put("tipo", transaction.type.toString())
                                .put("descricao", transaction.description)
                                .put("realizada_em", transaction.carriedOutAt.toString())
                        }
                    )
                    .encode()
            )
}