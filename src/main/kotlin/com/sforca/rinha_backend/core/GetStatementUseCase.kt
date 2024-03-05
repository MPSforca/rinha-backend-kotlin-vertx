package com.sforca.rinha_backend.core

import com.sforca.rinha_backend.core.output.BalanceOutput
import com.sforca.rinha_backend.core.output.GetStatementOutput
import com.sforca.rinha_backend.core.output.TransactionOutput
import com.sforca.rinha_backend.repository.client.Balance
import com.sforca.rinha_backend.repository.client.Transaction
import com.sforca.rinha_backend.repository.client.cache.StatementCache
import com.sforca.rinha_backend.repository.clientsRepository
import io.vertx.core.Future
import java.time.LocalDateTime

class GetStatementUseCase(private val cache: StatementCache) {

    operator fun invoke(clientId: Long): Future<GetStatementOutput> = cache.get(clientId)
}
