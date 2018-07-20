package io.openfuture.chain.network.client.handler

import io.openfuture.chain.network.base.handler.BaseHandler
import io.openfuture.chain.network.domain.Packet

abstract class ClientHandler<T : Packet> : BaseHandler<T>()