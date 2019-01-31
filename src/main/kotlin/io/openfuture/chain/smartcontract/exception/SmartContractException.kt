package io.openfuture.chain.smartcontract.exception

open class SmartContractException(message: String?) : RuntimeException(message)

class SmartContractClassCastException(message: String) : SmartContractException(message)