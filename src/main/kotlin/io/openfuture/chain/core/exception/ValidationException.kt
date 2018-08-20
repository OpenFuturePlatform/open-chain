package io.openfuture.chain.core.exception

import io.openfuture.chain.rpc.domain.ExceptionResponseField

class ValidationException(
    message: String?,
    val field: ExceptionResponseField? = null
) : RuntimeException(message)