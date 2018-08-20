package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.exception.model.ExceptionField

class ExceptionResponse(
    val status: Int,
    val message: String?,
    val field: ExceptionField? = null
)