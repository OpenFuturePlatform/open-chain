package io.openfuture.chain.core.exception

import io.openfuture.chain.core.exception.model.ExceptionType

class ValidationException(
    message: String?,
    val type: ExceptionType? = null
) : RuntimeException(message)