package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
        private val repository: TransactionRepository,
        private val voteTransactionRepository: VoteTransactionRepository,
        private val nodeClock: NodeClock,
        private val delegateService: AccountService
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<Transaction> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
            ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun addToBlock(hash: String, block: Block): Transaction {
        val persistTransaction = this.get(hash)
        if (null != persistTransaction.block) {
            throw LogicException("Transaction with hash: $hash already belong to block!")
        }

        persistTransaction.block = block
        return repository.save(persistTransaction)
    }

    // --  votes logic // todo need to create TransactionVoteService with extends this
    @Transactional
    override fun addVote(dto: VoteTransactionDto): Transaction {
        //todo need to add validation
        val vote = VoteDto(dto.senderKey, dto.delegateKey, dto.voteType, dto.weight) //todo need to think about calculate the vote weight
        delegateService.updateRatingByVote(vote)
        return repository.save(dto.toEntity())
    }

    override fun createVote(data: VoteTransactionData): VoteTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = TransactionUtils.calculateHash(networkTime, data)
        return VoteTransactionDto(networkTime, data.amount, data.recipientKey, data.senderKey, data.senderSignature,
                hash, data.voteType, data.delegateKey, data.weight)
    }

}