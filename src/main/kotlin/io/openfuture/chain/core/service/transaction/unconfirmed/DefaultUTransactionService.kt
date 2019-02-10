package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.UTransactionService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultUTransactionService<uT : UnconfirmedTransaction, uR : UTransactionRepository<uT>>(
    private val uRepository: uR
) : UTransactionService<uT> {

    override fun findByHash(hash: String): uT? = uRepository.findOneByHash(hash)

    override fun getAll(): List<uT> = uRepository.findAll()

    override fun getAll(request: PageRequest): List<uT> = uRepository.findAllByOrderByFeeDesc(request)

    override fun getAllBySenderAddress(address: String): List<uT> = uRepository.findAllBySenderAddress(address)

    override fun save(uTx: uT): uT = uRepository.save(uTx)

    override fun remove(uTx: uT) {
        uRepository.delete(uTx)
    }

}