package io.openfuture.chain.service

import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletService(
    private val repository: WalletRepository,
    private val consensusProperties: ConsensusProperties
) : WalletService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    @Transactional(readOnly = true)
    override fun getByAddress(address: String): Wallet = repository.findOneByAddress(address)
        ?: throw NotFoundException("Wallet with address: $address not exist!")

    @Transactional(readOnly = true)
    override fun getBalance(address: String): Long =
        repository.findOneByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE

    @Transactional
    override fun save(wallet: Wallet) {
        repository.save(wallet)
    }

    @Transactional
    override fun updateBalance(from: String, to: String, amount: Long) {
        updateByAddress(from, -amount)
        updateByAddress(to, amount)
    }

    @Transactional
    override fun changeWalletVote(address: String, delegate: Delegate, type: VoteType) {
        when (type) {
            VoteType.FOR -> {
                addVote(address, delegate)
            }
            VoteType.AGAINST -> {
                removeVote(address, delegate)
            }
        }
    }

    private fun addVote(address: String, delegate: Delegate) {
        val wallet = getByAddress(address)

        if (consensusProperties.delegatesCount!! <= wallet.votes.size) {
            throw IllegalStateException("Wallet $address already spent all votes!")
        }

        if (wallet.votes.contains(delegate)) {
            throw IllegalStateException("Wallet $address already voted for delegate with key ${delegate.publicKey}!")
        }

        wallet.votes.add(delegate)
        repository.save(wallet)
    }

    private fun removeVote(address: String, delegate: Delegate) {
        val wallet = getByAddress(address)

        if (!wallet.votes.contains(delegate)) {
            throw IllegalStateException("Wallet $address haven't vote for delegate with key ${delegate.publicKey}!")
        }

        wallet.votes.remove(delegate)
        repository.save(wallet)
    }

    private fun updateByAddress(address: String, amount: Long) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)
        wallet.balance += amount
        repository.save(wallet)
    }

}
