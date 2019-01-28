package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DeployTransactionPayload(

    @Column(name = "bytecode", nullable = false)
    var bytecode: ByteArray

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(bytecode.size + LONG_BYTES)
        buffer.put(bytecode)
        return buffer.array()
    }

}