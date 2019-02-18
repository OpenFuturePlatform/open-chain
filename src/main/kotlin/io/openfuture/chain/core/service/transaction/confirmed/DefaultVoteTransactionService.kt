package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultVoteTransactionService(
    private val repository: VoteTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : DefaultExternalTransactionService<VoteTransaction>(repository), VoteTransactionService {

    override fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction =
        repository.findFirstBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeIdOrderByTimestampDesc(
            senderAddress, delegateKey, FOR.getId())
            ?: throw NotFoundException("Last vote for delegate transaction not found")

    @Transactional
    override fun commit(tx: VoteTransaction, receipt: Receipt): VoteTransaction {
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

    override fun process(tx: VoteTransaction, delegateWallet: String): Receipt {
        val type = VoteType.values().first { it.getId() == tx.getPayload().voteTypeId }
        stateManager.updateVoteByAddress(tx.senderAddress, tx.getPayload().delegateKey, type)
        stateManager.updateWalletBalanceByAddress(tx.senderAddress, -tx.fee)
        stateManager.updateWalletBalanceByAddress(delegateWallet, tx.fee)

        return generateReceipt(type, tx, delegateWallet)
    }

    private fun generateReceipt(type: VoteType, tx: VoteTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(tx.senderAddress, consensusProperties.genesisAddress!!, 0, "$type ${tx.getPayload().delegateKey}"),
            ReceiptResult(tx.senderAddress, delegateWallet, tx.fee)
        )
        return getReceipt(tx.hash, results)
    }

}