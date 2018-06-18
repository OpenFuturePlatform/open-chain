package io.openfuture.chain.domain

import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.util.AppContextUtils

abstract class BaseNodeResponse(
        val version: String = AppContextUtils.getBean(NodeProperties::class.java).version!!
)