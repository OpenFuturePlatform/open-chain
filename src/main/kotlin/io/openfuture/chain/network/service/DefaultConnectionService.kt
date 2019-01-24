package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Service
class DefaultConnectionService(
    @Lazy private val bootstrap: Bootstrap,
    private val addressesHolder: AddressesHolder
) : ConnectionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }

    override fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>?): Boolean {
        var result: Boolean
        try {
            val future = bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
                if (future.isSuccess) {
                    onConnect?.let {
                        val channel = (future as ChannelFuture).channel()
                        it.accept(channel)
                    }
                } else {
                    log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                    addressesHolder.removeNodeInfo(networkAddress)
                    result = false
                }
            }
            future.await(5, TimeUnit.SECONDS)
            result = future.isSuccess
        } catch (ex: Exception) {
            result = false
        }
        return result
    }

}