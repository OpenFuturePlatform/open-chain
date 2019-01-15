package io.openfuture.chain.core.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.State
import io.openfuture.chain.core.model.entity.State.WalletSnapshot
import javax.persistence.AttributeConverter

class StateConverter : AttributeConverter<State.WalletSnapshot, String> {

    override fun convertToDatabaseColumn(attribute: State.WalletSnapshot): String =
        jacksonObjectMapper().writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): State.WalletSnapshot =
        jacksonObjectMapper().readValue(dbData, WalletSnapshot::class.java)

}