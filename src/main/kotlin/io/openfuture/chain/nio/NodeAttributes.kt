package io.openfuture.chain.nio

import io.openfuture.chain.property.NodeProperties
import org.springframework.stereotype.Component

@Component
class NodeAttributes(
    properties: NodeProperties
) {

    @Volatile
    var id : String? = null

    @Volatile
    var host: String? = null

    val port: Int = properties.port!!

    val version: String = properties.version!!
}