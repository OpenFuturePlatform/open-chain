package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.SyncBlockRequestMessage
import io.openfuture.chain.network.message.network.FindAddressesMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.server.TcpServer
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
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
                future.channel().writeAndFlush(FindAddressesMessage())

                future.channel().writeAndFlush(getNetworkBlockRequest())
            } else {
                log.warn("Can not connect to ${address.host}:${address.port}")
            }
        }
    }

    override fun broadcast(message: BaseMessage) {
        connectionService.getConnections().keys.forEach {
            it.writeAndFlush(message)
        }
    }

    private fun getNetworkBlockRequest(): SyncBlockRequestMessage {
        val lastBlockHash = blockService.getLast().hash

        return SyncBlockRequestMessage(lastBlockHash)
    }

}
