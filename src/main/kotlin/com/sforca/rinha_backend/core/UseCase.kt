package com.sforca.rinha_backend.core

interface UseCase<T, K> {
  operator fun invoke(input: T): K
}
