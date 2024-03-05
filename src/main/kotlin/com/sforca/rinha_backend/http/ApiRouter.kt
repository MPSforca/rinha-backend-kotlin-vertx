package com.sforca.rinha_backend.http

import com.sforca.rinha_backend.core.GetStatementUseCase
import com.sforca.rinha_backend.core.SaveTransactionUseCase
import com.sforca.rinha_backend.core.exception.ClientIdNotFoundException
import com.sforca.rinha_backend.core.exception.InconsistentTransactionValueException
import com.sforca.rinha_backend.http.handler.StatementHandler
import com.sforca.rinha_backend.http.handler.TransactionHandler
import com.sforca.rinha_backend.http.validation.StatementValidationHandler
import com.sforca.rinha_backend.http.validation.TransactionValidationHandler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.BodyProcessorException
import io.vertx.ext.web.validation.BodyProcessorException.BodyProcessorErrorType
import io.vertx.json.schema.ValidationException

class ApiRouter(
    private val vertx: Vertx,
    saveTransactionUseCase: SaveTransactionUseCase,
    getStatementUseCase: GetStatementUseCase,
) {

    private val transactionValidationHandler = TransactionValidationHandler(vertx)
    private val transactionHandler = TransactionHandler(saveTransactionUseCase)

    private val statementValidationHandler = StatementValidationHandler(vertx)
    private val statementHandler = StatementHandler(getStatementUseCase)

    fun router(): Router = Router.router(vertx).also {
        it.mapSaveTransactionEndpoint()
        it.mapGetStatementEndpoint()
    }

    private fun Router.mapSaveTransactionEndpoint() {
        this.post("/clientes/:id/transacoes")
            .handler(BodyHandler.create())
            .handler(transactionValidationHandler.saveRequestValidationHandler())
            .handler(transactionHandler::save)
            .failureHandler {
                val throwable = it.failure()
                print(throwable)
                val statusCode = when (throwable) {
                    is ClientIdNotFoundException -> 404
                    is ValidationException,
                    is BodyProcessorException,
                    is InconsistentTransactionValueException -> 422
                    else -> 500
                }
                it.response()
                    .setStatusCode(statusCode)
                    .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                    .end(JsonObject().put("error", throwable.message).encode())
            }
    }

    private fun Router.mapGetStatementEndpoint() {
        this.get("/clientes/:id/extrato")
            .handler(statementValidationHandler.getRequestValidationHandler())
            .handler(statementHandler::get)
            .failureHandler {
                val throwable = it.failure()
                val statusCode = when (throwable) {
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
}