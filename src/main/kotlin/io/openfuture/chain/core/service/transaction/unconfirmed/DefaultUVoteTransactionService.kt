package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.service.UVoteTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUVoteTransactionService(
    private val uRepository: UVoteTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : DefaultUTransactionService<UnconfirmedVoteTransaction, UVoteTransactionRepository>(uRepository),
    UVoteTransactionService {

    override fun getBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction? {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findOneBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(senderAddress, delegateKey, AGAINST.getId())
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun process(uTx: UnconfirmedVoteTransaction, delegateWallet: String): Receipt {
        val type = VoteType.values().first { it.getId() == uTx.getPayload().voteTypeId }
        stateManager.updateVoteByAddress(uTx.senderAddress, uTx.getPayload().delegateKey, type)
        stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -uTx.fee)

        return generateReceipt(type, uTx, delegateWallet)
    }

    private fun generateReceipt(type: VoteType, uTx: UnconfirmedVoteTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(uTx.senderAddress, consensusProperties.genesisAddress!!, 0, "$type ${uTx.getPayload().delegateKey}"),
            ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee)
        )
        return getReceipt(uTx.hash, results)
    }

}