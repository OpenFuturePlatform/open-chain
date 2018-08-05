package io.openfuture.chain.core.model.entity.transaction.payload

import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DelegateTransactionPayload(

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        return delegateKey.toByteArray(UTF_8)
    }

}