package io.openfuture.chain.rpc.domain

class ExceptionResponse(
    val status: Int,
    val message: String?,
    val type: String? = null
)