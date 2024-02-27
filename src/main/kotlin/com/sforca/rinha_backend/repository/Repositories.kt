package com.sforca.rinha_backend.repository

import com.sforca.rinha_backend.repository.client.ClientsRepository
import com.sforca.rinha_backend.repository.client.postgres.PostgresClientRepository

val clientsRepository: ClientsRepository = PostgresClientRepository()
