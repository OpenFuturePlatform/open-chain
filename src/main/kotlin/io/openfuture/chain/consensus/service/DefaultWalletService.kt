package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.Wallet
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.repository.WalletRepository
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
    override fun getBalanceByAddress(address: String): Long =
        repository.findOneByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE

    @Transactional(readOnly = true)
    override fun getVotesByAddress(address: String): MutableSet<Delegate> = this.getByAddress(address).votes

    @Transactional
    override fun save(wallet: Wallet) {
        repository.save(wallet)
    }

    @Transactional
    override fun updateBalance(from: String, to: String, amount: Long, fee: Long) {
        if (consensusProperties.genesisAddress!! != from) {
            updateByAddress(from, -(amount + fee))
        }
        updateByAddress(to, amount)
    }

    private fun updateByAddress(address: String, amount: Long) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)
        wallet.balance += amount
        repository.save(wallet)
    }

}
