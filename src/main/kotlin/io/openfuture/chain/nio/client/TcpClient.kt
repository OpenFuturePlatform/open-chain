package io.openfuture.chain.nio.client

import io.netty.bootstrap.Bootstrap
import io.openfuture.chain.nio.client.listener.ConnectionListener
import io.openfuture.chain.nio.client.service.TimeSynchronizationClient

/**
 * @author Evgeni Krylov
 */
class TcpClient(
    private val bootstrap: Bootstrap,
    private val host: String,
    private val port: Int,
    private val timeSynchronizationClient: TimeSynchronizationClient
) : Runnable {

    override fun run() {
        val future = bootstrap.connect(host, port)
        future.addListener(ConnectionListener(this, timeSynchronizationClient))
    }

}