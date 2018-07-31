package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.network.domain.FindAddresses
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.domain.NetworkBlockRequest
import io.openfuture.chain.network.domain.Packet
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.server.TcpServer
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.Executors

@Service
class DefaultNetworkService(
    private val bootstrap: Bootstrap,
    private val blockService: CommonBlockService,
    private val tcpServer: TcpServer,
    private val properties: NodeProperties,
    private val connectionService: ConnectionService
) : NetworkService, ApplicationListener<ApplicationReadyEvent> {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultNetworkService::class.java)
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        Executors.newSingleThreadExecutor().execute(tcpServer)

        val address = properties.getRootAddresses().shuffled(SecureRandom()).first()
        bootstrap.connect(address.host, address.port).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(FindAddresses())

                future.channel().writeAndFlush(getNetworkBlockRequest())
            } else {
                log.warn("Can not connect to ${address.host}:${address.port}")
            }
        }
    }

    @Scheduled(cron = "*/30 * * * * *")
    override fun maintainConnectionNumber() {
        if (isConnectionNeeded()) {
            requestAddresses()
        }
    }

    override fun broadcast(packet: Packet) {
        connectionService.getConnections().keys.forEach {
            it.writeAndFlush(packet)
        }
    }

    override fun connect(peers: List<NetworkAddress>) {
        peers.map { NetworkAddress(it.host, it.port) }
            .filter { !connectionService.getConnectionAddresses().contains(it) && it != NetworkAddress(properties.host!!,
                properties.port!!) }
            .forEach { bootstrap.connect(it.host, it.port) }
    }

    private fun isConnectionNeeded(): Boolean = properties.peersNumber!! > connectionService.getConnections().size

    private fun getNetworkBlockRequest(): NetworkBlockRequest {
        val lastBlockHash = blockService.getLast().hash

        return NetworkBlockRequest(lastBlockHash)
    }

    private fun requestAddresses() {
        send(FindAddresses())
    }

    private fun send(message: Packet) {
        val address = connectionService.getConnectionAddresses().shuffled(SecureRandom()).firstOrNull()
            ?: properties.getRootAddresses().shuffled().first()

        val channel = connectionService.getConnections().filter { it.value == address }.map { it.key }.firstOrNull()
            ?: bootstrap.connect(address.host, address.port).channel()
        channel.writeAndFlush(message)
    }

}
