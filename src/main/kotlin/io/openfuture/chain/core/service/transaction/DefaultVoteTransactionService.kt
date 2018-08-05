package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
internal class DefaultVoteTransactionService(
    repository: TransactionRepository<VoteTransaction>,
    uRepository: UTransactionRepository<UVoteTransaction>,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties
) : BaseTransactionService<VoteTransaction, UVoteTransaction>(repository, uRepository), VoteTransactionService {

    @Transactional
    override fun add(message: VoteTransactionMessage): UVoteTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UVoteTransaction.of(message)
        }

        val tx = UVoteTransaction.of(message)
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }

        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UVoteTransaction {
        val tx = UVoteTransaction.of(clock.networkTime(), request)
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }

        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun toBlock(utx: UVoteTransaction, block: MainBlock): VoteTransaction {
        val type = utx.payload.getVoteType()
        updateWalletVotes(utx.payload.delegateKey, utx.senderAddress, type)
        return super.toBlock(utx, VoteTransaction.of(utx), block)
    }

    private fun isValid(tx: UVoteTransaction): Boolean {
        return isValidVoteCount(tx.senderAddress) && super.isValid(tx)
    }

    private fun updateWalletVotes(delegateKey: String, senderAddress: String, type: VoteType) {
        val delegate = delegateService.getByPublicKey(delegateKey)
        val wallet = walletService.getByAddress(senderAddress)

        when (type) {
            VoteType.FOR -> {
                wallet.votes.add(delegate)
            }
            VoteType.AGAINST -> {
                wallet.votes.remove(delegate)
            }
        }
        walletService.save(wallet)
    }

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = uRepository.findAll()
            .filter { it.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}