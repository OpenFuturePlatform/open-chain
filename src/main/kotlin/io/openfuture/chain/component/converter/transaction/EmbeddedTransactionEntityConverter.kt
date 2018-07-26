package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction

abstract class EmbeddedTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : BaseTransactionEntityConverter<Entity, Data>() {

    abstract fun toEntity(timestamp: Long, data: Data): Entity

    protected fun getHash(data: Data, publicKey: ByteArray, privateKey: ByteArray): String {
        val signature = getSignature(data, privateKey)
        val bytes = getBytes(publicKey, signature.toByteArray(), data.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}