package io.openfuture.chain.smartcontract.core.service

import io.openfuture.chain.smartcontract.core.model.SmartContract

interface ContractService {

    fun deploy(contract: SmartContract)

    fun get(address: String): SmartContract

}