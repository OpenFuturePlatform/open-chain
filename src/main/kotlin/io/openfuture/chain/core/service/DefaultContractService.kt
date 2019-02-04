package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.repository.ContractRepository
import io.openfuture.chain.crypto.util.AddressUtils
import io.openfuture.chain.crypto.util.HashUtils.keccak256
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
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
    override fun generateAddress(owner: String): String {
        val nonce = getAllByOwner(owner).size
        val hash = keccak256((fromHexString(owner) + nonce.toByte()))
        return AddressUtils.bytesToAddress(hash)
    }

}