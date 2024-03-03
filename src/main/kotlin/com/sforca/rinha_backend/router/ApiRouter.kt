package com.sforca.rinha_backend.router

import com.sforca.rinha_backend.core.exception.ClientIdNotFoundException
import com.sforca.rinha_backend.core.exception.InconsistentTransactionValueException
import com.sforca.rinha_backend.core.exception.InvalidDescriptionLengthException
import com.sforca.rinha_backend.core.getStatementUseCase
import com.sforca.rinha_backend.core.input.SaveTransactionInput
import com.sforca.rinha_backend.core.input.TransactionType
import com.sforca.rinha_backend.core.saveTransactionUseCase
import com.sforca.rinha_backend.router.exception.InvalidLongException
import com.sforca.rinha_backend.router.exception.InvalidTransactionTypeException
import com.sforca.rinha_backend.router.exception.NullFieldException
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

val restApi: Router = Router.router(Vertx.vertx())

private val transactionRoute: Route = restApi.post("/clientes/:id/transacoes")
  .handler(BodyHandler.create())
  .handler {
    val body = it.body().asJsonObject()
    saveTransactionUseCase(
      SaveTransactionInput(
        clientId = it.pathParam("id").toLong(),
        value = body.getLongOrThrow("valor") ?: throw NullFieldException(),
        type = transactionTypeFromChar(body.getString("tipo").first()),
        description = body.getString("descricao") ?: throw NullFieldException()
      )
    )
      .onFailure { exception ->
        when (exception) {
          is ClientIdNotFoundException -> it.fail(404, exception)
          is InvalidDescriptionLengthException -> it.fail(422, exception)
          else -> it.fail(500, exception)
        }
      }
      .onSuccess { output ->
        it.response()
          .putHeader("content-type", "application/json")
          .end(
            JsonObject()
              .put("limite", output.limit)
              .put("saldo", output.value)
              .encode()
          )
      }
  }
  .failureHandler {
    val statusCode = when (it.failure()) {
      is InconsistentTransactionValueException,
      is NullFieldException,
      is InvalidLongException,
      is InvalidTransactionTypeException -> 422

      else -> it.statusCode()
    }
    it.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(statusCode)
      .end(JsonObject().put("message", it.failure().message ?: "an error occurred").encode())
  }

private fun JsonObject.getLongOrThrow(key: String): Long? = getNumber(key)?.let {
  if (it.toDouble() % 1 == 0.0) it.toLong() else throw InvalidLongException()
}

private fun transactionTypeFromChar(char: Char?): TransactionType = when (char) {
  'c' -> TransactionType.CREDIT
  'd' -> TransactionType.DEBIT
  else -> throw InvalidTransactionTypeException()
}

private val statementRoute: Route = restApi.get("/clientes/:id/extrato")
  .handler(BodyHandler.create())
  .handler {
    getStatementUseCase(it.pathParam("id").toLong())
      .onFailure { exception ->
        when (exception) {
          is ClientIdNotFoundException -> it.fail(404, exception)
          else -> it.fail(500, exception)
        }
      }
      .onSuccess { output ->
        it.response()
          .putHeader("content-type", "application/json")
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
  }
  .failureHandler {
    it.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(it.statusCode())
      .end(JsonObject().put("message", it.failure().message ?: "internal server error").encode())
  }
