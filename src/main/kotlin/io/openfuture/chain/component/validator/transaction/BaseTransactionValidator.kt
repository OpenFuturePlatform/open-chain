package io.openfuture.chain.component.validator.transaction

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.service.WalletService

abstract class BaseTransactionValidator<Entity : BaseTransaction, Data : BaseTransactionData>(
    private val walletService: WalletService
) : TransactionValidator<Entity, Data> {

    protected fun commonValidation(data: Data, signature: String, publicKey: String) {
        if (!isValidaSignature(data, signature, publicKey)) {
            throw ValidationException("Invalid transaction signature")
        }

        if (!isValidSenderBalance(data.senderAddress, data.amount)) {
            throw ValidationException("Invalid sender balance")
        }
    }

    private fun isValidaSignature(data: Data, signature: String, publicKey: String): Boolean {
        return SignatureManager.verify(data.getBytes(), signature, HashUtils.fromHexString(publicKey))
    }

    private fun isValidSenderBalance(senderAddress: String, amount: Long): Boolean {
        val balance = walletService.getUnspentBalance(senderAddress)
        if (balance < amount) {
            return false
        }
        return true
    }

}