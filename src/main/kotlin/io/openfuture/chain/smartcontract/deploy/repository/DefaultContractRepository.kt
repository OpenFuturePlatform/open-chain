package io.openfuture.chain.smartcontract.deploy.repository

import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import org.springframework.stereotype.Repository

@Repository
class DefaultContractRepository : ContractRepository {

    override fun get(address: String): ContractDto {
        TODO("not implemented")
    }

    override fun save(contact: ContractDto) {
        TODO("not implemented")
    }


}