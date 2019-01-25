package io.openfuture.chain.core.exception

open class CoreException(message: String) : RuntimeException(message)

class NotFoundVoteTypeException(message: String) : CoreException(message)