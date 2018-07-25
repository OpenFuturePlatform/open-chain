package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository
) : DefaultTransactionService<TransferTransaction, UTransferTransaction>(repository),
    TransferTransactionService {
    override fun add(uTx: UTransferTransaction): TransferTransaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toBlock(tx: TransferTransaction, block: MainBlock): TransferTransaction {
        return baseToBlock(tx, block)
    }

}