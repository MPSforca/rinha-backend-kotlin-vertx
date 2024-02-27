package com.sforca.rinha_backend.core

import com.sforca.rinha_backend.core.output.BalanceOutput
import com.sforca.rinha_backend.core.output.GetStatementOutput
import com.sforca.rinha_backend.core.output.TransactionOutput
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.Transaction
import com.sforca.rinha_backend.repository.clientsRepository
import io.vertx.core.Future
import java.time.LocalDateTime

class GetStatementUseCase {

  operator fun invoke(clientId: Long): Future<GetStatementOutput> =
    Future.all(
      clientsRepository.getBalanceForUpdate(clientId),
      clientsRepository.getLastTransactions(clientId, 10)
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
