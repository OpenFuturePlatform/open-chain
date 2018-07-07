package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.VoteTransaction
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
        private val nodeClock: NodeClock
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): List<Transaction> {
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

    @Transactional
    override fun add(dto: VoteTransactionDto): Transaction {
        return repository.save(VoteTransaction.of(dto))
    }

    override fun createVote(data: VoteTransactionData): VoteTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = TransactionUtils.calculateHash(data)
        return VoteTransactionDto(networkTime, data.amount, data.recipientKey, data.senderKey, data.senderSignature,
                hash, data.votes)
    }

}