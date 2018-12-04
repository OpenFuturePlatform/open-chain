package io.openfuture.chain.smartcontract.deploy.exception

class ContractExecutionException(
    message: String? = "Contract execution failed",
    cause: Throwable? = null
) : RuntimeException(message, cause)