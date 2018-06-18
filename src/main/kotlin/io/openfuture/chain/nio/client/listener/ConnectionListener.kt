package io.openfuture.chain.nio.client.listener

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.openfuture.chain.nio.client.TcpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ConnectionListener(
    private val tcpClient: TcpClient
) : ChannelFutureListener {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun operationComplete(future: ChannelFuture) {
        if (!future.isSuccess) {
            log.info("Reconnect...")
            future.channel().eventLoop().schedule(tcpClient, 5, TimeUnit.SECONDS)
        } else {
            log.info("Connected to ${future.channel().remoteAddress()}")
        }
    }

}