package io.openfuture.chain.core.model.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.state.payload.WalletPayload
import javax.persistence.AttributeConverter

class WalletPayloadConverter : AttributeConverter<WalletPayload, String> {

    override fun convertToDatabaseColumn(attribute: WalletPayload): String =
        jacksonObjectMapper().writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): WalletPayload =
        jacksonObjectMapper().readValue(dbData, WalletPayload::class.java)

}