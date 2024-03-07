package com.sforca.rinha.repository

import com.sforca.rinha.repository.client.ClientsRepository
import com.sforca.rinha.repository.client.postgres.PostgresClientRepository

val clientsRepository: ClientsRepository = PostgresClientRepository()
