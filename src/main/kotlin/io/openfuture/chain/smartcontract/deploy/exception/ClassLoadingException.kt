package io.openfuture.chain.smartcontract.deploy.exception

class ClassLoadingException(
    message: String? = "Class loading failed",
    cause: Throwable? = null
) : RuntimeException(message, cause)