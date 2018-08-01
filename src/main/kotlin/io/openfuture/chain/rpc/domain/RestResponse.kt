package io.openfuture.chain.rpc.domain

data class RestResponse<T>(
    val timestamp: Long,
    val version: String,
    val payload: T
)