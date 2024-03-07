package com.sforca.rinha.core.exception

import java.lang.RuntimeException

class InconsistentTransactionValueException :
    RuntimeException("The transaction value is inconsistent with the client limit")
