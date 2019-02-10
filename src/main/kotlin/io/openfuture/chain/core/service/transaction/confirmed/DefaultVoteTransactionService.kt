package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.dictionary.VoteType.values
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.UVoteTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultVoteTransactionService(
    private val repository: VoteTransactionRepository,
    private val uVoteTransactionService: UVoteTransactionService,
    private val consensusProperties: ConsensusProperties
) : DefaultExternalTransactionService<VoteTransaction, UnconfirmedVoteTransaction, VoteTransactionRepository,
    UVoteTransactionService>(repository, uVoteTransactionService), VoteTransactionService {

    @Transactional
    override fun commit(tx: VoteTransaction, receipt: Receipt): VoteTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            val utx = uVoteTransactionService.findByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(uTx: UnconfirmedVoteTransaction, delegateWallet: String): Receipt {
        val type = values().first { it.getId() == uTx.getPayload().voteTypeId }
        stateManager.updateVoteByAddress(uTx.senderAddress, uTx.getPayload().delegateKey, type)
        stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -uTx.fee)

        return generateReceipt(type, uTx, delegateWallet)
    }

    override fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction =
        repository.findFirstBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeIdOrderByTimestampDesc(senderAddress,
            delegateKey, FOR.getId())
            ?: throw NotFoundException("Last vote for delegate transaction not found")

    private fun generateReceipt(type: VoteType, uTx: UnconfirmedVoteTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(uTx.senderAddress, consensusProperties.genesisAddress!!, 0, "$type ${uTx.getPayload().delegateKey}"),
            ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee)
        )
        return getReceipt(uTx.hash, results)
    }

}