package io.openfuture.chain.network.base.handler

import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.domain.Packet

abstract class CommonHandler<T : Packet> : SimpleChannelInboundHandler<T>()