package io.openfuture.chain.service

import io.openfuture.chain.protocol.CommunicationProtocol

interface TimeSyncService {

    fun sync()

    fun calculateAndAddTimeOffset(packet: CommunicationProtocol.Packet, remoteAddress: String)

}