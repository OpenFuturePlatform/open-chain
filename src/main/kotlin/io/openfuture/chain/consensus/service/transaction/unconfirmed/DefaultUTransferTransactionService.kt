package io.openfuture.chain.consensus.service.transaction.unconfirmed

import io.openfuture.chain.consensus.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.consensus.repository.UTransferTransactionRepository
import io.openfuture.chain.consensus.service.UTransferTransactionService
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultUTransferTransactionService(
    repository: UTransferTransactionRepository
) : DefaultUTransactionService<UTransferTransaction, TransferTransactionData, TransferTransactionDto, TransferTransactionRequest>(repository),
    UTransferTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): UTransferTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<UTransferTransaction> = repository.findAll().toMutableSet()

    @Transactional
    override fun add(dto: TransferTransactionDto): UTransferTransaction {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }
        validate(dto)
        return saveAndBroadcast(dto.toUEntity())
    }

    @Transactional
    override fun add(request: TransferTransactionRequest): UTransferTransaction {
        validate(request)
        return saveAndBroadcast(request.toEntity(nodeClock.networkTime()))
    }

}