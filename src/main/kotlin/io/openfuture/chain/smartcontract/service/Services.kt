package io.openfuture.chain.smartcontract.service

import io.openfuture.chain.smartcontract.model.Contract
import io.openfuture.chain.smartcontract.model.ContractMethod

interface ContractService {

    fun invoke(address: String, method: ContractMethod)

    fun deploy(contract: Contract)

    fun get(address: String): Contract

}