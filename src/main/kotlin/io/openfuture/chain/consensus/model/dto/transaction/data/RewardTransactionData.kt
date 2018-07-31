package io.openfuture.chain.consensus.model.dto.transaction.data

import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.consensus.model.entity.transaction.RewardTransaction
import io.openfuture.chain.consensus.util.TransactionUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

@NoArgConstructor
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
        ByteUtils.toHexString(publicKey),
        SignatureUtils.sign(getBytes(), privateKey),
        TransactionUtils.createHash(this, publicKey, privateKey)
    )

}