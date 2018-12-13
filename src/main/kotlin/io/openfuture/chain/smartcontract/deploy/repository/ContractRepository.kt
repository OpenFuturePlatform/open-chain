package io.openfuture.chain.smartcontract.deploy.repository

import io.openfuture.chain.smartcontract.deploy.domain.ContractDto

interface ContractRepository {

    fun get(address: String): ContractDto
}
