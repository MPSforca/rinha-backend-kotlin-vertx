package com.sforca.rinha.core.wrapper

import com.sforca.rinha.core.SaveTransactionUseCase
import com.sforca.rinha.core.entity.Balance
import com.sforca.rinha.core.input.SaveTransactionInput
import com.sforca.rinha.core.repository.BalanceRepository
import com.sforca.rinha.core.repository.TransactionRepository
import io.vertx.core.Future
import io.vertx.sqlclient.Pool

class SaveTransactionWithCacheWrapper(
    private val statementCache: StatementCache,
    pool: Pool,
    balanceRepository: BalanceRepository,
    transactionRepository: TransactionRepository,
) : SaveTransactionUseCase(pool, balanceRepository, transactionRepository) {
    override fun invoke(input: SaveTransactionInput): Future<Balance> =
        super.invoke(input)
            .onSuccess {
                statementCache.invalidate(clientId = input.clientId)
            }
}
