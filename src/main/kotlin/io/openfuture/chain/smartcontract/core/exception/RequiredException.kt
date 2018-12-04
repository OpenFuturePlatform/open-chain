package io.openfuture.chain.smartcontract.core.exception

class RequiredException(message: String?) : SmartContractException(message ?: "Required Exception")