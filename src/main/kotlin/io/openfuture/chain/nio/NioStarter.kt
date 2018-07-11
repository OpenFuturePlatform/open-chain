package io.openfuture.chain.nio

import io.openfuture.chain.nio.server.TcpServer
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class NioStarter(
    private val tcpServer: TcpServer
) : ApplicationListener<ApplicationReadyEvent> {

    private val serverExecutor = Executors.newSingleThreadExecutor()

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        // start server
        serverExecutor.execute(tcpServer)

    }

}