package io.openfuture.chain.nio.base

import io.netty.util.AttributeKey
import io.openfuture.chain.entity.Node

object ChannelAttributes {
    val NODE_KEY: AttributeKey<Node> = AttributeKey.valueOf("NODE_KEY")
}