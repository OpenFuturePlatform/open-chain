package io.openfuture.chain.core.model.entity.block.payload

import com.fasterxml.jackson.annotation.JsonIgnore

interface BlockPayload {

    @JsonIgnore
    fun getBytes(): ByteArray

}
