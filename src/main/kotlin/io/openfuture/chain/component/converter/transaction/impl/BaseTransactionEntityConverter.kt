package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import java.nio.ByteBuffer

abstract class BaseTransactionEntityConverter<Entity : BaseTransaction, Data: BaseTransactionData> {

    abstract fun toEntity(dto: BaseTransactionDto<Data>): Entity

    protected fun getSignature(data: Data, privateKey: ByteArray): String {
        return SignatureManager.sign(data.getBytes(), privateKey)
    }

    protected fun getBytes(publicKey: ByteArray, signature: ByteArray, data: ByteArray): ByteArray {
        return ByteBuffer.allocate(data.size + publicKey.size + signature.size)
            .put(data)
            .put(publicKey)
            .put(signature)
            .array()
    }

}