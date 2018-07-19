package io.openfuture.chain.controller.common

import java.io.Serializable

data class RestResponse(
    val header: ResponseHeader,
    val body: Any?
) : Serializable