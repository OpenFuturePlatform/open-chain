package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    walletService: WalletService,
    private val nodeClock: NodeClock,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<VoteTransaction>(repository, walletService), VoteTransactionService {

    @Transactional
    override fun add(dto: VoteTransactionDto) {
        //todo need to add validation
        //todo need to think about calculate the vote weight
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return
        }

        saveAndBroadcast(VoteTransaction.of(dto))
    }

    @Transactional
    override fun add(request: VoteTransactionRequest) {
        saveAndBroadcast(VoteTransaction.of(nodeClock.networkTime(), request))
    }

    private fun saveAndBroadcast(tx: VoteTransaction) {
        repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

    override fun beforeAddToBlock(tx: VoteTransaction) {
        updateWalletVotes(tx.senderAddress, tx.delegateKey, tx.getVoteType())
        updateWalletBalance(tx.senderAddress, tx.recipientAddress, tx.amount)
    }

    private fun updateWalletVotes(address: String, delegateKey: String, type: VoteType) {
        val delegate = delegateService.getByPublicKey(delegateKey)
        when (type) {
            VoteType.FOR -> {
                walletService.addVote(address, delegate)
            }
            VoteType.AGAINST -> {
                walletService.removeVote(address, delegate)
            }
        }
    }

}