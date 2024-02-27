package com.sforca.rinha_backend.core.exception

import java.lang.RuntimeException

class InconsistentTransactionValueException :
  RuntimeException("The transaction value is inconsistent with the client limit")
