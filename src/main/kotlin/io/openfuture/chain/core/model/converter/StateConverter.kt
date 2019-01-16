package io.openfuture.chain.core.model.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.State
import io.openfuture.chain.core.model.entity.State.Data
import javax.persistence.AttributeConverter

class StateConverter : AttributeConverter<State.Data, String> {

    override fun convertToDatabaseColumn(attribute: State.Data): String =
        jacksonObjectMapper().writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): State.Data =
        jacksonObjectMapper().readValue(dbData, Data::class.java)

}