package io.openfuture.chain.smartcontract.exception

class RequiredException(message: String?) : SmartContractException(message ?: "Required Exception")