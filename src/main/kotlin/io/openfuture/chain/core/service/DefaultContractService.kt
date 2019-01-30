package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.repository.ContractRepository
import io.openfuture.chain.smartcontract.core.utils.HashUtils.keccak256
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultContractService(
    private val repository: ContractRepository
) : ContractService {

    @Transactional(readOnly = true)
    override fun getByAddress(address: String): Contract = repository.findOneByAddress(address)
        ?: throw NotFoundException("Contract with address: $address doesn't exist!")

    @Transactional(readOnly = true)
    override fun getAllByOwner(owner: String): List<Contract> = repository.findAllByOwner(owner)

    @Transactional
    override fun save(contract: Contract): Contract = repository.save(contract)

    @Transactional(readOnly = true)
    override fun generateAddress(owner: String): String =
        toHexString(keccak256((fromHexString(owner) + getAllByOwner(owner).size.toByte())))

}