package io.openfuture.chain.network.server.handler

import io.openfuture.chain.network.base.handler.BaseHandler
import io.openfuture.chain.network.domain.Packet

abstract class ServerHandler<T : Packet> : BaseHandler<T>()