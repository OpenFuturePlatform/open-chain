package io.openfuture.chain.core.model.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.State
import io.openfuture.chain.core.model.entity.State.Snapshot
import javax.persistence.AttributeConverter

class StateConverter : AttributeConverter<State.Snapshot, String> {

    override fun convertToDatabaseColumn(attribute: State.Snapshot): String =
        jacksonObjectMapper().writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): State.Snapshot =
        jacksonObjectMapper().readValue(dbData, Snapshot::class.java)

}