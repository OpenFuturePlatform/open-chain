package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletService(
    private val repository: WalletRepository
) : WalletService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    @Transactional(readOnly = true)
    override fun getByAddress(address: String): Wallet = repository.findOneByAddress(address)
        ?: throw NotFoundException("Wallet with address: $address not found")

    @Transactional(readOnly = true)
    override fun getBalanceByAddress(address: String): Long =
        repository.findOneByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE

    override fun getUnspentBalanceByAddress(address: String): Long {
        val wallet = getByAddress(address)
        return wallet.balance - wallet.uncomfirmedOut

    }

    @Transactional(readOnly = true)
    override fun getVotesByAddress(address: String): MutableSet<Delegate> = this.getByAddress(address).votes

    @Transactional
    override fun save(wallet: Wallet) {
        repository.save(wallet)
    }

    fun updateBalanceByInput(address: String, amount: Long) {
        updateByAddress()
    }

    fun updateBalanceByOut() {

    }

    fun updateUnconfirmedByOut() {

    }


    override fun updateBalanceByFee(address: String, fee: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Transactional
    override fun updateBalance(from: String, to: String, amount: Long, fee: Long) {
        updateByAddress(from, -(amount + fee))
        updateByAddress(to, amount)
    }

    override fun updateUnconfirmedOut(address: String, fee: Long) {
        updateByAddress(address, fee)
    }

    private fun updateByAddress(address: String, amount: Long) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)
        wallet.balance += amount
        repository.save(wallet)
    }

}
