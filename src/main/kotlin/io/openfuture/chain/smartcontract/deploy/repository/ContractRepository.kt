package io.openfuture.chain.smartcontract.deploy.repository

import io.openfuture.chain.smartcontract.deploy.domain.ContractDto

//will extend JPA repository
interface ContractRepository {

    fun get(address: String): ContractDto

    fun save(contact: ContractDto)

}
