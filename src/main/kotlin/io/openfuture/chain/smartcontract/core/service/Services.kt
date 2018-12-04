package io.openfuture.chain.smartcontract.core.service

import io.openfuture.chain.smartcontract.core.model.SmartContract

interface ContractService {

    fun get(address: String): SmartContract

}

interface TransactionService {

    fun transfer(senderAddress: String, recipientAddress: String, amount: Long)

}

interface BlockService {

    fun blockHash(height: Long): String

    fun blockTimestamp(height: Long): String

}