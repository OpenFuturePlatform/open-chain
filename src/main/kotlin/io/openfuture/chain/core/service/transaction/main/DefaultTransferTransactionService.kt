package io.openfuture.chain.core.service.transaction.main

import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository
) : BaseTransactionService<TransferTransaction, UTransferTransaction>(repository, uRepository),
    TransferTransactionService {

    override fun toBlock(dto: TransferTransactionDto, block: MainBlock) {
        processAndSave(dto.toEntity(), block)
    }

    override fun toBlock(hash: String, block: MainBlock) {
        val tx = getUnconfirmed(hash)
        val newTx = tx.toConfirmed()
        super.processAndSave(newTx, block)
    }

}