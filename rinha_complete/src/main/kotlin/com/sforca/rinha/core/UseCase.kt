package com.sforca.rinha.core

interface UseCase<T, K> {
    fun invoke(input: T): K
}
