package com.sforca.rinha.core

import com.sforca.rinha.core.output.GetStatementOutput
import com.sforca.rinha.repository.client.cache.StatementCache
import io.vertx.core.Future

class GetStatementUseCase(private val cache: StatementCache) {
    operator fun invoke(clientId: Long): Future<GetStatementOutput> = cache.get(clientId)
}
