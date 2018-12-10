package io.openfuture.chain.smartcontract.repository

import io.openfuture.chain.smartcontract.deploy.domain.ContractDto

interface ContractRepository {

    fun get(address: String): ContractDto
}
