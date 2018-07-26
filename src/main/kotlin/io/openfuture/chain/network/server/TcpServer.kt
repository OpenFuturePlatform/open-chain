package io.openfuture.chain.network.server

import io.netty.bootstrap.ServerBootstrap
import io.openfuture.chain.property.NodeProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TcpServer(
    private val bootstrap: ServerBootstrap,
    private val property: NodeProperty
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(TcpServer::class.java)
    }


    override fun run() {
        try {
            val future = bootstrap.bind(property.port!!)
            log.info("Netty started on port: ${property.port}")

            future.sync()
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Server in trouble", e)
        }
    }

}