package io.openfuture.chain.core.model.entity.transaction.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

interface TransactionPayload : Serializable {

    @JsonIgnore
    fun getBytes(): ByteArray

}