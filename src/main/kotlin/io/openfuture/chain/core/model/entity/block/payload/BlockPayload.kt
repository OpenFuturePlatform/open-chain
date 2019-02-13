package io.openfuture.chain.core.model.entity.block.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

interface BlockPayload : Serializable {

    @JsonIgnore
    fun getBytes(): ByteArray

}
