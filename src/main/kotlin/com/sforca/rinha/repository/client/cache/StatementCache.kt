package com.sforca.rinha.repository.client.cache

import com.sforca.rinha.core.output.GetStatementOutput
import io.vertx.core.Future

interface StatementCache {
    fun get(clientId: Long): Future<GetStatementOutput>

    fun invalidate(clientId: Long)
}
