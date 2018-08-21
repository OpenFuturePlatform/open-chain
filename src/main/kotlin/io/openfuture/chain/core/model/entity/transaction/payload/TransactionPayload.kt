package io.openfuture.chain.core.model.entity.transaction.payload

import com.fasterxml.jackson.annotation.JsonIgnore

interface TransactionPayload {

    @JsonIgnore
    fun getBytes(): ByteArray

}