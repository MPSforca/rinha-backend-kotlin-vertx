package com.sforca.rinha.http.handler

import com.sforca.rinha.core.GetStatementUseCase
import com.sforca.rinha.core.output.GetStatementOutput
import com.sforca.rinha.http.APPLICATION_JSON
import com.sforca.rinha.http.CONTENT_TYPE_HEADER
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class StatementHandler(
    private val getStatement: GetStatementUseCase,
) {
    fun get(rc: RoutingContext): Future<GetStatementOutput> =
        getStatement(clientId = rc.pathParam("id").toLong())
            .onSuccess { getStatementSuccessResponse(rc, it) }
            .onFailure { rc.fail(it) }

    private fun getStatementSuccessResponse(
        rc: RoutingContext,
        output: GetStatementOutput,
    ) = rc.response()
        .setStatusCode(200)
        .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
        .end(output.toJson().encode())

    private fun GetStatementOutput.toJson(): JsonObject =
        json {
            obj(
                "saldo" to
                    obj(
                        "total" to balance.value,
                        "data_extrato" to checkDate.toString(),
                        "limite" to balance.limit,
                    ),
                "ultimas_transacoes" to
                    lastTransactions.map {
                        obj(
                            "valor" to it.value,
                            "tipo" to it.type.toString(),
                            "descricao" to it.description,
                            "realizada_em" to it.carriedOutAt.toString(),
                        )
                    },
            )
        }
}
