package io.openfuture.chain.network

import io.openfuture.chain.network.server.TcpServer
import io.openfuture.chain.service.NetworkService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class NetworkStarter(
    private val tcpServer: TcpServer,
    private val networkService: NetworkService
    ) : ApplicationListener<ApplicationReadyEvent> {

        private val serverExecutor = Executors.newSingleThreadExecutor()

        override fun onApplicationEvent(event: ApplicationReadyEvent) {
            serverExecutor.execute(tcpServer)

            networkService.start()
        }

    }