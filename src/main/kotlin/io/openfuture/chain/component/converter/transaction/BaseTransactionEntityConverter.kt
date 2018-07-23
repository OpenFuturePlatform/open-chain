package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import java.nio.ByteBuffer

abstract class BaseTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData> : TransactionEntityConverter<Entity, Data> {

    protected fun getHash(request: BaseTransactionRequest<Data>): String {
        return HashUtils.toHexString(HashUtils.doubleSha256(getBytes(request.senderPublicKey!!.toByteArray(),
            request.senderSignature!!.toByteArray(), request.data!!.getBytes())))
    }

    private fun getBytes(publicKey: ByteArray, signature: ByteArray, data: ByteArray): ByteArray {
        return ByteBuffer.allocate(data.size + publicKey.size + signature.size)
            .put(data)
            .put(publicKey)
            .put(signature)
            .array()
    }

}