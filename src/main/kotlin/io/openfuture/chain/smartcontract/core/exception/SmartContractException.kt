package io.openfuture.chain.smartcontract.core.exception

abstract class SmartContractException(message: String) : RuntimeException("Smart contract exception: $message")