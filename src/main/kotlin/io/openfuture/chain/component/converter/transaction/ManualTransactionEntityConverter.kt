package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.component.converter.transaction.impl.BaseTransactionEntityConverter
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction

abstract class ManualTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : BaseTransactionEntityConverter<Entity, Data>() {

    abstract fun toEntity(timestamp: Long, request: BaseTransactionRequest<Data>): Entity

    protected fun getHash(request: BaseTransactionRequest<Data>): String {
        val bytes = getBytes(request.senderPublicKey!!.toByteArray(),
            request.senderSignature!!.toByteArray(), request.data!!.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}