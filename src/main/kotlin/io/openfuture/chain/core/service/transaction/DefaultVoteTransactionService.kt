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
    private val repository: TransactionRepository<VoteTransaction>,
    private val uRepository: UTransactionRepository<UVoteTransaction>,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties
) : BaseTransactionService(), VoteTransactionService {

    @Transactional
    override fun add(message: VoteTransactionMessage): UVoteTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UVoteTransaction.of(message)
        }

        val tx = UVoteTransaction.of(message)
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UVoteTransaction {
        val tx = request.toUEntity(clock.networkTime())
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun toBlock(utx: UVoteTransaction, block: MainBlock) {
        val type = utx.payload.getVoteType()
        updateWalletVotes(utx.payload.delegateKey, utx.senderAddress, type)

        val tx = VoteTransaction.of(utx)
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(utx)
        repository.save(tx)
    }

    @Transactional
    fun validate(tx: UVoteTransaction) {
        if (!isValidVoteCount(tx.senderAddress)) {
            throw ValidationException("Wallet ${tx.senderAddress} already spent all votes!")
        }
        super.validate(tx)
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