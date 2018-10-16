package io.openfuture.chain.smartcontract.exception

abstract class SmartContractException(message: String) : RuntimeException("Smart contract exception: $message")