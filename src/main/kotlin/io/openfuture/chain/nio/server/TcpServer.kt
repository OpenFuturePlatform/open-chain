package io.openfuture.chain.nio.server

import io.netty.bootstrap.ServerBootstrap
import io.openfuture.chain.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TcpServer(
    private val serverBootstrap: ServerBootstrap,
    private val properties: NodeProperties
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun run() {
        try {
            val future = serverBootstrap.bind(properties.port!!)
            log.info("Netty server is started on port: ${properties.port}")

            future.sync()
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Server in trouble", e)
        }
    }

}