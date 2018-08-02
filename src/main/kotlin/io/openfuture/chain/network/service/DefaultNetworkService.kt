package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.network.message.application.block.BlockRequestMessage
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.address.NetworkAddressMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.server.TcpServer
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.Executors

@Service
@Lazy(true)
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
                future.channel().writeAndFlush(FindAddressesMessage())

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

    override fun broadcast(message: BaseMessage) {
        connectionService.getConnections().keys.forEach {
            it.writeAndFlush(message)
        }
    }

    override fun connect(peers: List<NetworkAddressMessage>) {
        peers.map { NetworkAddressMessage(it.host, it.port) }
            .filter { !connectionService.getConnectionAddresses().contains(it) && it != NetworkAddressMessage(properties.host!!,
                properties.port!!)
            }
            .forEach { bootstrap.connect(it.host, it.port) }
    }

    private fun isConnectionNeeded(): Boolean = properties.peersNumber!! > connectionService.getInboundConnections().size

    private fun getNetworkBlockRequest(): BlockRequestMessage {
        val lastBlockHash = blockService.getLast().hash

        return BlockRequestMessage(lastBlockHash)
    }

    private fun requestAddresses() {
        val address = connectionService.getConnectionAddresses().shuffled(SecureRandom()).firstOrNull()
            ?: properties.getRootAddresses().shuffled().first()
        send(address, FindAddressesMessage())
    }

    private fun send(address: NetworkAddressMessage, message: BaseMessage) {
        val channel = connectionService.getConnections().filter { it.value == address }.map { it.key }.firstOrNull()
            ?: bootstrap.connect(address.host, address.port).addListener { future ->
                future as ChannelFuture
                if (future.isSuccess) {
                    future.channel().writeAndFlush(message)
                } else {
                    log.warn("Can not connect to ${address.host}:${address.port}")
                }
            }.channel()
        channel.writeAndFlush(message)
    }

}
