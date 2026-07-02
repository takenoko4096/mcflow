package io.github.takenoko4096.bcl

abstract class BclException protected constructor(message: String, cause: Throwable?) : RuntimeException(message, cause)
