package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateTransactionService(
    private val repository: DelegateTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : DefaultExternalTransactionService<DelegateTransaction>(repository), DelegateTransactionService {

    @Transactional
    override fun commit(tx: DelegateTransaction, receipt: Receipt): DelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            val utx = uRepository.findOneByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(tx: DelegateTransaction, delegateWallet: String): Receipt {
        stateManager.updateWalletBalanceByAddress(tx.senderAddress, -(tx.getPayload().amount + tx.fee))
        stateManager.updateWalletBalanceByAddress(consensusProperties.genesisAddress!!, tx.getPayload().amount)
        stateManager.addDelegate(tx.getPayload().delegateKey, tx.senderAddress, tx.timestamp)
        stateManager.updateWalletBalanceByAddress(delegateWallet, tx.fee)

        return generateReceipt(tx, delegateWallet)
    }

    private fun generateReceipt(tx: DelegateTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(tx.senderAddress, consensusProperties.genesisAddress!!, tx.getPayload().amount,
                tx.getPayload().delegateKey),
            ReceiptResult(tx.senderAddress, delegateWallet, tx.fee)
        )
        return getReceipt(tx.hash, results)
    }

}