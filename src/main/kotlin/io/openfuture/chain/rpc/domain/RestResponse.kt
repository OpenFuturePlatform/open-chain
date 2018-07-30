package io.openfuture.chain.rpc.domain

data class RestResponse<T>(
    val header: ResponseHeader,
    val body: T
)