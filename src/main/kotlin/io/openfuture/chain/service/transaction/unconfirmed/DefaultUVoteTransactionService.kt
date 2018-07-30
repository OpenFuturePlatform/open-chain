package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.UVoteTransactionRepository
import io.openfuture.chain.service.UVoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultUVoteTransactionService(
    repository: UVoteTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : DefaultUTransactionService<UVoteTransaction, VoteTransactionData, VoteTransactionDto, VoteTransactionRequest>(repository),
    UVoteTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): UVoteTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<UVoteTransaction> = repository.findAll().toMutableSet()

    @Transactional
    override fun add(dto: VoteTransactionDto): UVoteTransaction {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }
        validate(dto)
        return saveAndBroadcast(dto.toUEntity())
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UVoteTransaction {
        validate(request)
        return saveAndBroadcast(request.toEntity(nodeClock.networkTime()))
    }

    @Transactional
    override fun validate(request: VoteTransactionRequest) {
        if (!isValidVoteCount(request.data!!.senderAddress)) {
            throw ValidationException("Wallet ${request.data!!.senderAddress} already spent all votes!")
        }

        super.validate(request)
    }

    @Transactional
    override fun validate(dto: VoteTransactionDto) {
        if (!isValidVoteCount(dto.data.senderAddress)) {
            throw ValidationException("Wallet ${dto.data.senderAddress} already spent all votes!")
        }

        super.validate(dto)
    }

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = getAll()
            .filter { it.senderAddress == senderAddress && it.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}