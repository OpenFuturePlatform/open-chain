package io.openfuture.chain.core.model.entity.transaction.payload

import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DelegateTransactionPayload(

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String,

    @Column(name = "delegate_host", nullable = false, unique = true)
    var delegateHost: String,

    @Column(name = "delegate_port", nullable = false, unique = true)
    var delegatePort: Int

) : TransactionPayload {

    override fun getBytes(): ByteArray = (delegateKey + delegateHost + delegatePort).toByteArray(UTF_8)

}