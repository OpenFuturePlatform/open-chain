package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import java.nio.ByteBuffer

abstract class BaseTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : TransactionEntityConverter<Entity, Data> {

    protected fun getHash(request: BaseTransactionRequest<Data>): String {
        val bytes = getBytes(request.senderPublicKey!!.toByteArray(),
            request.senderSignature!!.toByteArray(), request.data!!.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    protected fun getHash(data: Data, publicKey: ByteArray, privateKey: ByteArray): String {
        val signature = getSignature(data, privateKey)
        val bytes = getBytes(publicKey, signature.toByteArray(), data.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    protected fun getSignature(data: Data, privateKey: ByteArray): String {
        return SignatureManager.sign(data.getBytes(), privateKey)
    }

    private fun getBytes(publicKey: ByteArray, signature: ByteArray, data: ByteArray): ByteArray {
        return ByteBuffer.allocate(data.size + publicKey.size + signature.size)
            .put(data)
            .put(publicKey)
            .put(signature)
            .array()
    }

}