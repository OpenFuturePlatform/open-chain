package io.openfuture.chain.smartcontract.deploy.exception

class ContractLoadingException(
    message: String? = "Class loading failed",
    cause: Throwable? = null
) : RuntimeException(message, cause)