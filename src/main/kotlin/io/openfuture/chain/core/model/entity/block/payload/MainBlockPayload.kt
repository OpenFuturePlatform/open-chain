package io.openfuture.chain.core.model.entity.block.payload

import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class MainBlockPayload(

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String

) : BlockPayload {

    override fun getBytes(): ByteArray = merkleHash.toByteArray(UTF_8)

}