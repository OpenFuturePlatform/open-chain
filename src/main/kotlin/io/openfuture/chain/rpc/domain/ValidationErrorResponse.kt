package io.openfuture.chain.rpc.domain

data class ValidationErrorResponse(
    val field: String,
    val message: String
)