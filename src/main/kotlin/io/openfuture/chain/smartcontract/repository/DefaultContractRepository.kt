package io.openfuture.chain.smartcontract.repository

import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import org.springframework.stereotype.Repository

@Repository
class DefaultContractRepository : ContractRepository {

    override fun get(address: String): ContractDto {
        TODO("not implemented")
    }

}