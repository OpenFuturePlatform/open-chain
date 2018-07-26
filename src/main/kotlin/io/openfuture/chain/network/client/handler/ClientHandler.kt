package io.openfuture.chain.network.client.handler

import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.domain.Packet

abstract class ClientHandler<T : Packet> : SimpleChannelInboundHandler<T>()