package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val networkService: NetworkApiService
) : BaseTransactionService<VoteTransaction, UnconfirmedVoteTransaction>(repository, uRepository), VoteTransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction> = unconfirmedRepository.findAllByOrderByFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction {
        if (isExists(message.hash)) {
            return UnconfirmedVoteTransaction.of(message)
        }

        val savedUtx = super.save(UnconfirmedVoteTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        val uTransaction = UnconfirmedVoteTransaction.of(request)
        if (isExists(uTransaction.hash)) {
            return uTransaction
        }

        val savedUtx = super.save(uTransaction)
        networkService.broadcast(VoteTransactionMessage(savedUtx))
        return savedUtx
    }

    @Transactional
    override fun synchronize(message: VoteTransactionMessage, block: MainBlock) {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            confirm(utx, block)
            return
        }
        super.save(VoteTransaction.of(message, block))
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): VoteTransaction {
        val utx = getUnconfirmedByHash(hash)
        return confirm(utx, block)
    }

    @Transactional
    override fun validate(tx: VoteTransaction) {
        validateVoteCount(tx.senderAddress)
        super.validate(tx)
    }

    @Transactional
    override fun validate(utx: UnconfirmedVoteTransaction) {
        validateVoteCount(utx.senderAddress)
        super.validate(utx)
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

    private fun confirm(utx: UnconfirmedVoteTransaction, block: MainBlock): VoteTransaction {
        val type = utx.payload.getVoteType()
        updateWalletVotes(utx.payload.delegateKey, utx.senderAddress, type)
        return super.confirmProcess(utx, VoteTransaction.of(utx, block))
    }

    private fun validateVoteCount(senderAddress: String) {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = unconfirmedRepository.findAll()
            .filter { it.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        if (consensusProperties.delegatesCount!! <= confirmedVotes + unconfirmedForVotes) {
            throw ValidationException(TRANSACTION_EXCEPTION_MESSAGE + "count of votes bigger than delegates count")
        }
    }

}