package io.openfuture.chain.smartcontract.exception

class AssertionException(message: String?) : SmartContractException(message ?: "Assertion Exception")