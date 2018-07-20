package io.openfuture.chain.network.base.handler

import io.openfuture.chain.network.domain.Packet

abstract class CommonHandler<T : Packet> : BaseHandler<T>()