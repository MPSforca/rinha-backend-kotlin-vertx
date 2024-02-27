package com.sforca.rinha_backend.router.exception

import java.lang.RuntimeException

class InvalidTransactionTypeException: RuntimeException("Bad char used to represent transaction type")
