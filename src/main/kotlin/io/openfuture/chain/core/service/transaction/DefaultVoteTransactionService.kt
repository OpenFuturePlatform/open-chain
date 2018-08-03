package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
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

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UVoteTransaction> {
        return uRepository.findAll()
    }

    @Transactional
    override fun add(dto: VoteTransactionDto): UVoteTransaction {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return UVoteTransaction.of(dto)
        }

        val tx = UVoteTransaction.of(dto)
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
    override fun toBlock(hash: String, block: MainBlock) {
        val unconfirmedTx = getUnconfirmedByHash(hash)
        val type = unconfirmedTx.getPayload().getVoteType()
        updateWalletVotes(unconfirmedTx.getPayload().delegateKey, unconfirmedTx.senderAddress, type)

        val tx = unconfirmedTx.toConfirmed()
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(unconfirmedTx)
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
        val unconfirmedForVotes = getAllUnconfirmed()
            .filter { it.senderAddress == senderAddress && it.getPayload().getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

    private fun getUnconfirmedByHash(hash: String): UVoteTransaction = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed vote transaction with hash: $hash not found")

}