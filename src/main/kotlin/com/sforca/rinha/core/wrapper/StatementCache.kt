package com.sforca.rinha.core.wrapper

import com.sforca.rinha.core.entity.Statement
import io.vertx.core.Future

interface StatementCache {
    fun get(clientId: Long): Future<Statement?>

    fun save(statement: Statement)

    fun invalidate(clientId: Long)
}
