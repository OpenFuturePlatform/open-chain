package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.TransferTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: TransferTransactionEntityConverter
) : DefaultTransactionService<TransferTransaction, TransferTransactionData>(repository,
    walletService, nodeClock, entityConverter), TransferTransactionService {

    override fun addToBlock(tx: TransferTransaction, block: MainBlock): TransferTransaction {
        return this.commonAddToBlock(tx, block)
    }

}