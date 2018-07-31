package io.openfuture.chain.consensus.service.transaction

import io.openfuture.chain.consensus.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.consensus.repository.TransferTransactionRepository
import io.openfuture.chain.consensus.repository.UTransferTransactionRepository
import io.openfuture.chain.consensus.service.TransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository
) : DefaultTransactionService<TransferTransaction, UTransferTransaction>(repository, uRepository),
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