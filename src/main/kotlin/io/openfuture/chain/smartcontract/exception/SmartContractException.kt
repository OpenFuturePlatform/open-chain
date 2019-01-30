package io.openfuture.chain.smartcontract.exception

open class SmartContractException(message: String?) : RuntimeException(message)

class SmartContractValidationException(message: String) : SmartContractException(message)