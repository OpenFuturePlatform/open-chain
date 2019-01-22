package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class DelegateTransactionRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank @field:AddressChecksum var senderAddress: String? = null,
    @field:NotNull var amount: Long? = null,
    @field:NotBlank var nodeId: String? = null, //todo remove
    @field:NotBlank var nodeKey: String? = null,  // todo rename delegateKey
    @field:NotBlank var nodeHost: String? = null, //todo remove
    @field:NotNull var nodePort: Int? = null, //todo remove
    @field:NotBlank var hash: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
) {

    fun createHash(): String {
        val header = TransactionHeader(timestamp!!, fee!!, senderAddress!!)
        val payload = DelegateTransactionPayload(nodeKey!!, amount!!)
        val bytes = ByteBuffer.allocate(header.getBytes().size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    fun sign(privateKey: String): String =
        SignatureUtils.sign(ByteUtils.fromHexString(hash), ByteUtils.fromHexString(privateKey))

}