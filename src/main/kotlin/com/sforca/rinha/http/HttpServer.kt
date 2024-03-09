package com.sforca.rinha.http

import com.sforca.rinha.core.GetStatementUseCase
import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.core.exception.ClientIdNotFoundException
import com.sforca.rinha.core.exception.InconsistentTransactionValueException
import com.sforca.rinha.http.handler.StatementHandler
import com.sforca.rinha.http.handler.TransactionHandler
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.ext.web.validation.BodyProcessorException
import io.vertx.json.schema.ValidationException

class HttpServer(
    private val vertx: Vertx,
    saveTransactionUseCase: SaveTransactionUseCase,
    getStatementUseCase: GetStatementUseCase,
) {
    private val transactionHandler = TransactionHandler(saveTransactionUseCase)

    private val statementHandler = StatementHandler(getStatementUseCase)

    fun start(
        startPromise: Promise<Void>,
        port: Int,
    ): Future<RouterBuilder> =
        RouterBuilder.create(vertx, "openapi/specs.yaml")
            .onSuccess {
                it.saveTransactionOperation()
                it.getStatementOperation()
                vertx.createHttpServer()
                    .requestHandler(it.createRouter())
                    .listen(port) { http ->
                        if (http.succeeded()) {
                            startPromise.complete()
                            println("HTTP server started on port $port")
                        } else {
                            startPromise.fail(http.cause())
                        }
                    }
            }
            .onFailure {
                println("Failed to create router from Open API: $it")
            }

    private fun RouterBuilder.saveTransactionOperation() =
        operation("saveTransaction")
            .handler(transactionHandler::save)
            .failureHandler {
                val throwable = it.failure()
                print(throwable)
                val statusCode =
                    when (throwable) {
                        is ClientIdNotFoundException -> 404
                        is ValidationException,
                        is BodyProcessorException,
                        is InconsistentTransactionValueException,
                        -> 422

                        else -> 500
                    }
                it.response()
                    .setStatusCode(statusCode)
                    .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                    .end(JsonObject().put("error", throwable.message).encode())
            }

    private fun RouterBuilder.getStatementOperation() =
        operation("getStatement")
            .handler(statementHandler::get)
            .failureHandler {
                val throwable = it.failure()
                val statusCode =
                    when (throwable) {
                        is ClientIdNotFoundException -> 404
                        is ValidationException -> 422
                        else -> 500
                    }
                it.response()
                    .setStatusCode(statusCode)
                    .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                    .end(JsonObject().put("error", throwable.message).encode())
            }
}
