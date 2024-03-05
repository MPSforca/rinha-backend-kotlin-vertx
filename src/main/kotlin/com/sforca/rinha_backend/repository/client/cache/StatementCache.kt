package com.sforca.rinha_backend.repository.client.cache

import com.sforca.rinha_backend.core.output.GetStatementOutput
import io.vertx.core.Future

interface StatementCache {
    fun get(clientId: Long): Future<GetStatementOutput>

    fun invalidate(clientId: Long)
}