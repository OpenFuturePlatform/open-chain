package io.openfuture.chain.network.server

import io.netty.bootstrap.ServerBootstrap
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class TcpServer(
    private val bootstrap: ServerBootstrap,
    private val properties: NodeProperties,
    private val eventPublisher: ApplicationEventPublisher
) : ApplicationListener<ApplicationReadyEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TcpServer::class.java)
        private const val LOGO = """

              ______                                      _                       _  _                             _____    ______   _____    ______      _
             / _____)                _                   | |                     | || |                  _        / ___ \  / __   | / ___ \  / __   |    | |
            | /  ___   ___          | |_    ___          | |  ___    ____   ____ | || | _    ___    ___ | |_   _ ( (   ) )| | //| |( (   ) )| | //| |   / /
            | | (___) / _ \         |  _)  / _ \         | | / _ \  / ___) / _  || || || \  / _ \  /___)|  _) (_) > > < < | |// | | > > < < | |// | |  / /
            | \____/|| |_| |        | |__ | |_| |        | || |_| |( (___ ( ( | || || | | || |_| ||___ || |__  _ ( (___) )|  /__| |( (___) )|  /__| | / /
             \_____/  \___/          \___) \___/         |_| \___/  \____) \_||_||_||_| |_| \___/ (___/  \___)(_) \_____/  \_____/  \_____/  \_____/ |_|


            """
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        try {
            val channelFuture = bootstrap.bind(properties.port!!).sync()
            log.info("Netty started on port: ${properties.port}")
            log.info(LOGO)
            eventPublisher.publishEvent(ServerReadyEvent(this))

            channelFuture.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Server in trouble", e)
        }
    }

}

class ServerReadyEvent(source: Any): ApplicationEvent(source)