package io.openfuture.chain.core.exception

import io.openfuture.chain.core.exception.model.ExceptionField

class ValidationException(
    message: String?,
    val field: ExceptionField? = null
) : RuntimeException(message)