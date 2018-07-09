package io.openfuture.chain.nio

import io.openfuture.chain.nio.server.TcpServer
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.NetworkService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class NioStarter(
    private val tcpServer: TcpServer,
    private val networkService: NetworkService,
    private val nodeProperties: NodeProperties
) : ApplicationListener<ApplicationReadyEvent> {

    private val serverExecutor = Executors.newSingleThreadExecutor()

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        // start server
        serverExecutor.execute(tcpServer)

        // start clients
        val address = nodeProperties.rootNodes[0].split(":")
        networkService.joinNetwork(address[0], address[1].toInt())

        //TODO: remove sleep and replace it by callback
        Thread.sleep(1000)

        networkService.connect(address[0], address[1].toInt())

    }

}