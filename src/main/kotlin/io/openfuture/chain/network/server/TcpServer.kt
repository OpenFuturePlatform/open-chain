package io.openfuture.chain.network.server

import io.netty.bootstrap.ServerBootstrap
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TcpServer(
    private val bootstrap: ServerBootstrap,
    private val properties: NodeProperties
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(TcpServer::class.java)
        private const val LOGO = """

              ______                                      _                       _  _                             _____    ______   _____    ______      _
             / _____)                _                   | |                     | || |                  _        / ___ \  / __   | / ___ \  / __   |    | |
            | /  ___   ___          | |_    ___          | |  ___    ____   ____ | || | _    ___    ___ | |_   _ ( (   ) )| | //| |( (   ) )| | //| |   / /
            | | (___) / _ \         |  _)  / _ \         | | / _ \  / ___) / _  || || || \  / _ \  /___)|  _) (_) > > < < | |// | | > > < < | |// | |  / /
            | \____/|| |_| |        | |__ | |_| |        | || |_| |( (___ ( ( | || || | | || |_| ||___ || |__  _ ( (___) )|  /__| |( (___) )|  /__| | / /
             \_____/  \___/          \___) \___/         |_| \___/  \____) \_||_||_||_| |_| \___/ (___/  \___)(_) \_____/  \_____/  \_____/  \_____/ |_|


            """
    }


    override fun run() {
        try {
            val future = bootstrap.bind(properties.port!!)
            log.info("Netty started on port: ${properties.port}")
            log.info(LOGO)

            future.sync()
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Server in trouble", e)
        }
    }

}