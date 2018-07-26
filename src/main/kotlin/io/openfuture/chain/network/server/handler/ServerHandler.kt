package io.openfuture.chain.network.server.handler

import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.domain.Packet

abstract class ServerHandler<T : Packet> : SimpleChannelInboundHandler<T>()