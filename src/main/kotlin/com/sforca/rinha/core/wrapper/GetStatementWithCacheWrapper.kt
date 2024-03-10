package com.sforca.rinha.core.wrapper

import com.sforca.rinha.core.GetStatementUseCase
import com.sforca.rinha.core.entity.Statement
import com.sforca.rinha.core.repository.BalanceRepository
import com.sforca.rinha.core.repository.TransactionRepository
import io.vertx.core.Future

class GetStatementWithCacheWrapper(
    private val statementCache: StatementCache,
    balanceRepository: BalanceRepository,
    transactionRepository: TransactionRepository,
) : GetStatementUseCase(balanceRepository, transactionRepository) {
    override fun invoke(input: Long): Future<Statement> =
        statementCache.get(input)
            .flatMap {
                it?.let { Future.succeededFuture(it) }
                    ?: super.invoke(input).onSuccess { statement -> statementCache.save(statement) }
            }
}
