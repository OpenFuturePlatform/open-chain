package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.util.TransactionUtils
import java.security.PrivateKey
import java.security.PublicKey

class RewardTransactionData(
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String
) : BaseTransactionData(amount, fee, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        return builder.toString().toByteArray()
    }

    fun toEntity(timestamp: Long, publicKey: ByteArray, privateKey: ByteArray): RewardTransaction = RewardTransaction(
        timestamp,
        amount,
        fee,
        recipientAddress,
        senderAddress,
        HashUtils.toHexString(publicKey),
        SignatureManager.sign(getBytes(), privateKey),
        TransactionUtils.createHash(this, publicKey, privateKey)
    )

}