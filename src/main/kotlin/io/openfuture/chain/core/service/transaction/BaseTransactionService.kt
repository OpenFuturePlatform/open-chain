package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.core.BaseTransactionMessage
import io.openfuture.chain.network.service.NetworkService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseTransactionService<T : Transaction, U: UTransaction> (
    protected val repository: TransactionRepository<T>,
    protected val uRepository: UTransactionRepository<U>
) {

    @Autowired
    protected lateinit var clock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var cryptoService: CryptoService

    protected fun add(utx: U) : U {
        updateUnconfirmedBalanceByFee(utx)
        return uRepository.save(utx)
    }

    protected fun toBlock(utx: U, tx: T, block: MainBlock): T {
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(utx)
        return repository.save(tx)
    }

    protected fun updateBalanceByFee(tx: T) {
        walletService.decreaseBalance(tx.senderAddress, tx.fee)
    }

    protected fun updateUnconfirmedBalanceByFee(tx: U) {
        walletService.decreaseUnconfirmedBalance(tx.senderAddress, tx.fee)
    }

    protected fun isValid(tx: BaseTransaction): Boolean {
        return isValidAddress(tx.senderAddress, tx.senderPublicKey)
            && isValidFee(tx.senderAddress, tx.fee)
            && isValidHash(tx.timestamp, tx.fee, tx.getPayload(), tx.hash)
            && isValidaSignature(tx.hash, tx.senderSignature, tx.senderPublicKey)
    }

    private fun isValidAddress(senderAddress: String, senderPublicKey: String): Boolean {
        return !cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))
    }

    private fun isValidFee(senderAddress: String, amount: Long): Boolean {
        val unspentBalance = walletService.getUnspentBalanceByAddress(senderAddress)
        if (unspentBalance < amount) {
            return false
        }
        return true
    }

    private fun isValidHash(timestamp: Long, fee: Long, payload: TransactionPayload, hash: String): Boolean {
        return TransactionUtils.generateHash(timestamp, fee, payload) == hash
    }

    private fun isValidaSignature(hash: String, signature: String, publicKey: String): Boolean {
        return SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))
    }

}