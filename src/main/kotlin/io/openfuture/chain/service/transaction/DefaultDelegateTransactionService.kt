package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.DelegateTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.service.DelegateTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: DelegateTransactionEntityConverter
) : DefaultBaseTransactionService<DelegateTransaction, DelegateTransactionData>(repository, walletService, nodeClock,
    entityConverter), DelegateTransactionService {

    @Transactional
    override fun addToBlock(tx: DelegateTransaction, block: MainBlock): DelegateTransaction {
        return super.commonAddToBlock(tx, block)
    }

}