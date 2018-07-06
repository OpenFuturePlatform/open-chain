package io.openfuture.chain.domain.transaction.payload

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.openfuture.chain.entity.dictionary.TransactionPayloadType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = VotePayload::class, name = "VOTE"),
    JsonSubTypes.Type(value = DelegateRegistrationPayload::class, name = "DELEGATE_REGISTRATION")
)
abstract class TransactionPayload(
        val type: TransactionPayloadType
)