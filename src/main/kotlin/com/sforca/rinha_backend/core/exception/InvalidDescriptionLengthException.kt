package com.sforca.rinha_backend.core.exception

import java.lang.RuntimeException

class InvalidDescriptionLengthException: RuntimeException("The description must be within 1 and 10 characters")
