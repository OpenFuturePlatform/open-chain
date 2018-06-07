package io.openfuture.chain.nio

import io.netty.bootstrap.Bootstrap
import io.openfuture.chain.nio.client.TcpClient
import io.openfuture.chain.nio.client.service.TimeSynchronizationClient
import io.openfuture.chain.nio.server.TcpServer
import io.openfuture.chain.property.NodeProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

/**
 * @author Evgeni Krylov
 */
@Component
class NioStarter(
    private val tcpServer: TcpServer,
    private val clientBootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val timeSynchronizationClient: TimeSynchronizationClient
) : ApplicationListener<ApplicationReadyEvent> {

    private val serverExecutor = Executors.newSingleThreadExecutor()
    private val clientExecutors = Executors.newFixedThreadPool(nodeProperties.rootNodes.size)

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        // start server
        serverExecutor.execute(tcpServer)

        // start clients
        nodeProperties.rootNodes.forEach {
            val address = it.split(":")
            clientExecutors.execute(TcpClient(clientBootstrap, address[0], address[1].toInt(), timeSynchronizationClient))
        }

    }

}