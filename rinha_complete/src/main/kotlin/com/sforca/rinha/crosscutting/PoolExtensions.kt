package com.sforca.rinha.crosscutting

import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Transaction

fun <T> Pool.withTransaction(f: (SqlConnection, Transaction) -> Future<T>): Future<T> =
    connection
        .flatMap { conn ->
            conn.begin()
                .compose { tx ->
                    f(conn, tx)
                        .eventually(conn::close)
                }
        }
