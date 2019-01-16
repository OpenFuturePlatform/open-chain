package io.openfuture.chain.core.model.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.state.payload.NodePayload
import javax.persistence.AttributeConverter

class NodePayloadConverter : AttributeConverter<NodePayload, String> {

    override fun convertToDatabaseColumn(attribute: NodePayload): String =
        jacksonObjectMapper().writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): NodePayload =
        jacksonObjectMapper().readValue(dbData, NodePayload::class.java)

}