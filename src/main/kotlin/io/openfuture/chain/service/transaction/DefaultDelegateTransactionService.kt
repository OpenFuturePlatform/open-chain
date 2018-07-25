package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.service.DelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository
) : DefaultTransactionService<DelegateTransaction, UDelegateTransaction>(repository),
    DelegateTransactionService {

    override fun add(uTx: UDelegateTransaction): DelegateTransaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Transactional
    override fun toBlock(tx: DelegateTransaction, block: MainBlock): DelegateTransaction {
        return baseToBlock(tx, block)
    }


}