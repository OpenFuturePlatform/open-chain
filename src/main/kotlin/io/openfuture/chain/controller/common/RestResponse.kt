package io.openfuture.chain.controller.common

data class RestResponse<T>(
    val header: ResponseHeader,
    val body: T
)