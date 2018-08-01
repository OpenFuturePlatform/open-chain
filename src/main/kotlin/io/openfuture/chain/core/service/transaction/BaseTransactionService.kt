package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class BaseTransactionService {

    @Autowired
    protected lateinit var clock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    protected lateinit var cryptoService: CryptoService
//
//    protected fun getUnconfirmed(hash: String): UEntity = uRepository.findOneByHash(hash)
//        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not found")
//
//    protected fun processAndSave(tx: Entity, block: MainBlock) {
//        if (commonService.isExists(tx.hash)) {
//            return
//        }
//
//        tx.block = block
//        updateBalance(tx)
//        repository.save(tx)
//    }
//
//    private fun updateBalance(tx: Entity) {
//        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
//    }

    protected fun updateBalanceByFee(tx: Transaction) {
        walletService.updateBalance(tx.senderAddress, tx.getPayload().fee)
    }

    protected fun updateBalanceByFee(tx: UTransaction) {
        walletService.updateUnconfirmedOut(tx.senderAddress, tx.getPayload().fee)
    }

    @Transactional
    open fun validate(tx: BaseTransaction) {
        if (!isValidHash(tx.getPayload(), tx.senderPublicKey, tx.senderSignature, tx.hash)) {
            throw ValidationException("Invalid transaction hash")
        }

        if (!cryptoService.isValidAddress(tx.senderAddress, ByteUtils.fromHexString(tx.senderPublicKey))) {
            throw ValidationException("Address and public key are incompatible")
        }

        if (!isValidFee(tx.senderAddress, tx.getPayload().fee)) {
            throw ValidationException("Invalid wallet balance")
        }

        if (!isValidaSignature(tx.getPayload(), tx.senderPublicKey, tx.senderSignature)) {
            throw ValidationException("Invalid transaction signature")
        }
    }

    private fun isValidHash(payload: BaseTransactionPayload, publicKey: String, signature: String, hash: String): Boolean {
        return TransactionUtils.createHash(payload, publicKey, signature) == hash
    }

    private fun isValidaSignature(payload: BaseTransactionPayload, publicKey: String, signature: String): Boolean {
        return SignatureUtils.verify(payload.getBytes(), signature, ByteUtils.fromHexString(publicKey))
    }

    private fun isValidFee(senderAddress: String, amount: Long): Boolean {
        val unspentBalance = walletService.getUnspentBalanceByAddress(senderAddress)
        if (unspentBalance < amount) {
            return false
        }
        return true
    }

}