package io.openfuture.chain.smartcontract.service

import io.openfuture.chain.smartcontract.model.SmartContract

interface ContractService {

    fun deploy(contract: SmartContract)

    fun get(address: String): SmartContract

}