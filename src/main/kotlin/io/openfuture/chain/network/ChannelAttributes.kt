package io.openfuture.chain.network

import io.netty.util.AttributeKey
import io.openfuture.chain.network.domain.Peer

object ChannelAttributes {
    val REMOTE_PEER: AttributeKey<Peer> = AttributeKey.valueOf("REMOTE_PEER")
}
